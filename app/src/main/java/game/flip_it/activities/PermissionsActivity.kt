package game.flip_it.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import game.flip_it.R
import game.flip_it.databinding.ActivityPermissionsBinding
import game.flip_it.utils.showCustomDialog

class PermissionsActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: FirebaseFirestore

    // Replace with your logic to get a stable user id (e.g. FirebaseAuth.getInstance().uid)
    private val userId: String = "ID_MM_111"

    private val binding: ActivityPermissionsBinding by lazy {
        ActivityPermissionsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.lightestGrey)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = Firebase.firestore

        with(binding) {
            btnAllow.setOnClickListener {
                // Check permission on start (or call from onResume/onStart depending on your flow)
                checkLocationPermissionAndProceed()
            }
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
                showDialogForPermissionDenial()
            }
        }

    private fun showDialogForPermissionDenial() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permission Required")
            .setMessage("Location permission is necessary to get restaurants and fetch map data.\n\nPlease enable it from Settings.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private var isReturningFromSettings = false

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

        isReturningFromSettings = true   // Track lifecycle return
    }

    override fun onResume() {
        super.onResume()

        if (isReturningFromSettings) {
            isReturningFromSettings = false
            checkLocationPermissionAndProceed()
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
                        proceedToNextScreen()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error adding location doc", e)
                        proceedToNextScreen()
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
                        proceedToNextScreen()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error uploading fresh location", e)
                        proceedToNextScreen()
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

    private fun proceedToNextScreen() {
        startActivity(Intent(this@PermissionsActivity, OnboardingActivity::class.java))
        finish()
    }
}