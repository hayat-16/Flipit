package game.flip_it.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import game.flip_it.R
import game.flip_it.databinding.ActivitySplashBinding
import game.flip_it.utils.hideStatusBar

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            hideStatusBar()
            // Start playing the animation
            lottieAnimFlippingView.playAnimation()

            // Transition to Home after 4 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
                finish()
            }, 4000)
        }
    }
}
