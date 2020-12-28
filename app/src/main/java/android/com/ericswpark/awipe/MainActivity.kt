package android.com.ericswpark.awipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun enableCountField(v: View) {
        val editText = findViewById<EditText>(R.id.main_activity_run_multiple_count_edit_text)
        val checkBox = findViewById<CheckBox>(R.id.main_activity_run_multiple_checkbox)
        editText.isEnabled = checkBox.isChecked
        if (checkBox.isChecked) {
            editText.requestFocus()
        }
    }
}