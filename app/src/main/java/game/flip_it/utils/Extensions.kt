package game.flip_it.utils

import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.app.Dialog
import android.view.LayoutInflater
import android.view.Window
import android.media.AudioManager
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import game.flip_it.R

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun AppCompatActivity.hideStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11+ (API 30+)
        window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()
    }
}

/**
 * Extension to show or hide keyboard using a boolean flag.
 *
 * @param show Pass true to show the keyboard, false to hide it.
 */
fun AppCompatActivity.toggleKeyboard(show: Boolean, targetView: View? = currentFocus) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (show) {
        targetView?.let {
            it.requestFocus()
            imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
        }
    } else {
        targetView?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
    }
}

fun ensureMinVolume(context: Context, minLevel: Int = 5) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    if (currentVolume < minLevel) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            minLevel,
            AudioManager.FLAG_SHOW_UI // show system volume bar
        )
//        Toast.makeText(context, "Volume increased for better sound 🎵", Toast.LENGTH_SHORT).show()
    }
}

fun showCustomDialog(context: Context, message: String) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)

    val view = LayoutInflater.from(context).inflate(R.layout.dialog_info, null)
    val tvMessage = view.findViewById<TextView>(R.id.tvDialogMessage)
    val btnOk = view.findViewById<Button>(R.id.btnDialogOk)

    tvMessage.text = message

    btnOk.setOnClickListener {
        dialog.dismiss()
    }

    dialog.setContentView(view)

    // Making background transparent
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    // Setting width to 90% of screen
    val params: WindowManager.LayoutParams? = dialog.window?.attributes
    params?.width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
    dialog.window?.attributes = params

    dialog.show()
}



