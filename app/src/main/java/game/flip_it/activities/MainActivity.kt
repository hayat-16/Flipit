package game.flip_it.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import game.flip_it.databinding.ActivityMainBinding
import game.flip_it.utils.MyConstants
import game.flip_it.utils.showCustomDialog
import game.flip_it.utils.toggleKeyboard

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            btnFlipNow.setOnClickListener {
                validateInputs()
            }

            textInputLayout1.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    textInputLayout1.error = null
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })

            textInputLayout2.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    textInputLayout2.error = null
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })
        }
    }

    private fun ActivityMainBinding.validateInputs() {
        val optionOne = etOptionOne.text.toString().trim()
        val optionTwo = etOptionTwo.text.toString().trim()

        // clear old errors
        textInputLayout1.error = null
        textInputLayout2.error = null

        when {
            optionOne.isEmpty() -> {
                toggleKeyboard(true, etOptionOne)
                textInputLayout1.editText?.requestFocus()
                textInputLayout1.error = "This field is required!"
            }

            optionTwo.isEmpty() -> {
                toggleKeyboard(true, etOptionTwo)
                textInputLayout2.editText?.requestFocus()
                textInputLayout2.error = "This field is required!"
            }

            optionOne == optionTwo -> {
                toggleKeyboard(false, etOptionOne)
                showCustomDialog(this@MainActivity,
                    message = "Both options can’t be the same.\nPlease enter two different choices!"
                )
                return
            }

            else -> {
                // proceed to next screen
                // Navigate to card flipping screen
                toggleKeyboard(false)
                // starting new activity transition...
                startActivity(Intent(this@MainActivity, TossActivity::class.java)
                    .putExtra(MyConstants.KEY_FIRST_NAME, textInputLayout1.editText?.text.toString())
                    .putExtra(MyConstants.KEY_SECOND_NAME, textInputLayout2.editText?.text.toString()))
                // should finish or not...
            }
        }
    }
}