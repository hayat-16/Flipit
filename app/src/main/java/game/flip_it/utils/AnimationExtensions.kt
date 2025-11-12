package game.flip_it.utils

import android.animation.ObjectAnimator
import android.view.View

private var blinkAnimator: ObjectAnimator? = null

/**
 * Starts blinking animation on a View (e.g., TextView)
 */
fun View.startBlinking() {
    // Avoid multiple animators on same view
    if (blinkAnimator == null) {
        blinkAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f).apply {
            duration = 600
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    } else if (!(blinkAnimator?.isRunning == true)) {
        blinkAnimator?.start()
    }
}

/**
 * Stops blinking animation and resets alpha
 */
fun View.stopBlinking() {
    blinkAnimator?.cancel()
    blinkAnimator = null
    this.alpha = 1f // reset visibility
}
