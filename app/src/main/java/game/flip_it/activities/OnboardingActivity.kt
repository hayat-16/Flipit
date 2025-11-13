package game.flip_it.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import game.flip_it.R
import game.flip_it.databinding.ActivityOnboardingBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class OnboardingActivity : AppCompatActivity() {
    private val binding: ActivityOnboardingBinding by lazy {
        ActivityOnboardingBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: FirebaseFirestore

    // Replace with your logic to get a stable user id (e.g. FirebaseAuth.getInstance().uid)
    private val userId: String = "ID_MM_111"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.orange)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = Firebase.firestore

        with(binding) {
            btnStartGame.setOnClickListener {
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }

            // Check permission on start (or call from onResume/onStart depending on your flow)
            checkLocationPermissionAndProceed()
        }
    }

    private fun checkLocationPermissionAndProceed() {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            uploadLocationToFirestore()
        } else {
            // Use Activity Result API to request permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                uploadLocationToFirestore()
            } else {
                Log.w("LocationActivity", "Location permission denied")
                // Optionally show rationale or disable location features
            }
        }

    @SuppressLint("MissingPermission")
    private fun uploadLocationToFirestore() {
        // Try last known location first
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                val timestamp = System.currentTimeMillis()

                val data = mapOf(
                    "latitude" to lat,
                    "longitude" to lng,
                    "timestamp" to timestamp
                )

                // Option A: overwrite a user-specific document
//                db.collection("user_locations")
//                    .document(userId)
//                    .set(data)
//                    .addOnSuccessListener {
//                        Log.d("Firestore", "Location uploaded (document userId): $lat, $lng")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e("Firestore", "Error uploading location", e)
//                    }

                // Option B (alternative): add a new document each time (comment out if using A)
                db.collection("user_locations")
                    .add(data)
                    .addOnSuccessListener { docRef ->
                        Log.d("Firestore", "Location added (new doc): ${docRef.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error adding location doc", e)
                    }
            } else {
                // lastLocation was null — request a fresh single high-accuracy update
                requestNewLocationAndUpload()
            }
        }.addOnFailureListener { e ->
            Log.e("LocationActivity", "Failed to get last location", e)
            requestNewLocationAndUpload()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationAndUpload() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000L
        ).setMaxUpdates(1).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val loc = locationResult.lastLocation ?: return
                val lat = loc.latitude
                val lng = loc.longitude
                val timestamp = System.currentTimeMillis()

                val data = mapOf(
                    "latitude" to lat,
                    "longitude" to lng,
                    "timestamp" to timestamp
                )

                db.collection("user_locations")
                    .document(userId)
                    .set(data)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Location uploaded (fresh): $lat, $lng")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error uploading fresh location", e)
                    }

                // Remove updates for this callback (we used maxUpdates=1 but we still remove)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}