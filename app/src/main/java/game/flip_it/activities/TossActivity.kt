package game.flip_it.activities

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorInflater
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import game.flip_it.R
import game.flip_it.databinding.ActivityTossBinding
import game.flip_it.utils.MyConstants
import game.flip_it.utils.SoundManager
import game.flip_it.utils.hide
import game.flip_it.utils.show
import game.flip_it.utils.startBlinking
import game.flip_it.utils.stopBlinking
import kotlin.random.Random

class TossActivity : AppCompatActivity() {
    private var optionOne: String = ""
    private var optionTwo: String = ""

    private val binding: ActivityTossBinding by lazy {
        ActivityTossBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Get restaurant names from intent
        optionOne = intent.getStringExtra(MyConstants.KEY_FIRST_NAME) ?: "Error: Option 1"
        optionTwo = intent.getStringExtra(MyConstants.KEY_SECOND_NAME) ?: "Error: Option 2"

        with(binding) {
            cardOptionX.text = optionOne
            cardOptionY.text = optionTwo
            performFlip()

            btnFlipAgain.setOnClickListener {
                tvFlippingStatus.text = getString(R.string.txt_decision_pending)
                tvResult.hide()
                btnFlipAgain.hide()
                performFlip()
            }
        }
    }

    private fun ActivityTossBinding.performFlip() {
        // Play flip animation
        // flipping using object animator...
        val flip1 = AnimatorInflater.loadAnimator(this@TossActivity, R.animator.flip_cards)
        flip1.setTarget(cardOptionX)
        val flip2 = AnimatorInflater.loadAnimator(this@TossActivity, R.animator.flip_cards)
        flip2.setTarget(cardOptionY)

        flip1.start()
        flip2.start()

        flip2.addListener(object : AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                randomlyRevealResult()
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }

        })
    }

    private fun ActivityTossBinding.randomlyRevealResult() {
        // Randomly pick a winner
        val randomNum = Random.nextInt(3)
        tvMessage.hide()
        tvResult.hide()

        when (randomNum) {
            0 -> showResult(optionOne)
            1 -> showResult(optionTwo)
            else -> showTryAgain()
        }
    }

    private fun ActivityTossBinding.showResult(winner: String) {
        tvMessage.hide()
        tvMessage.stopBlinking()
        tvResult.text = "${getString(R.string.txt_winner)} $winner"
        tvResult.show()
        tvFlippingStatus.text = "Here we go! We have our decision made :)"

        SoundManager.playSound(this@TossActivity, R.raw.win)
    }

    private fun ActivityTossBinding.showTryAgain() {
        tvResult.hide()
        tvMessage.text = getString(R.string.txt_try_flipping_again)
        tvMessage.show()
        tvMessage.startBlinking()
        btnFlipAgain.show()
        tvFlippingStatus.text = "Bad Luck! Let's try again :)"
        SoundManager.playSound(this@TossActivity, R.raw.try_again)
    }
}