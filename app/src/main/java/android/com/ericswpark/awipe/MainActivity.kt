package android.com.ericswpark.awipe

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

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

    fun startWipeClicked(v: View) {
        hideSoftKeyBoard()

        // Verify count
        val editText = findViewById<EditText>(R.id.main_activity_run_multiple_count_edit_text)
        val checkBox = findViewById<CheckBox>(R.id.main_activity_run_multiple_checkbox)
        if (checkBox.isChecked && Integer.parseInt(editText.text.toString()) >= 3) {
            confirmLargeCount(v)
        } else {
            confirmWipe(v)
        }
    }

    private fun confirmLargeCount(v: View) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder
            .setTitle(R.string.main_activity_large_count_confirm_title)
            .setMessage(R.string.main_activity_large_count_confirm_message)
            .setPositiveButton(android.R.string.ok) { _, _ -> confirmWipe(v) }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun confirmWipe(v: View) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder
            .setTitle(R.string.main_activity_start_confirm_title)
            .setMessage(R.string.main_activity_start_confirm_message)
            .setPositiveButton(android.R.string.ok) { _, _ -> wipeRoutine(v) }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun wipeRoutine(v: View) {
        // On start, disable button
        val startButton = findViewById<Button>(R.id.main_activity_start_button)
        startButton.isEnabled = false

        Snackbar.make(v, R.string.main_activity_wipe_started, Snackbar.LENGTH_SHORT).show()
        vibratePhone(v.context)

        // Get run count
        val editText = findViewById<EditText>(R.id.main_activity_run_multiple_count_edit_text)
        val checkBox = findViewById<CheckBox>(R.id.main_activity_run_multiple_checkbox)

        if (checkBox.isChecked) {
            // Multiple runs
            val count = Integer.parseInt(editText.text.toString())

            for (i in 1..count) {
                wipe(v)
            }
        } else {
            wipe(v)
        }

        Snackbar.make(v, R.string.main_activity_wipe_finished, Snackbar.LENGTH_SHORT).show()
        vibratePhone(v.context)

        // On end, enable button
        startButton.isEnabled = true
    }

    private fun wipe(v: View) {
        // Query free space
        val availableBytes = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val storageManager = applicationContext.getSystemService(
                StorageManager::class.java
            )
            val appSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(filesDir)
            storageManager.getAllocatableBytes(appSpecificInternalDirUuid)
        } else {
            val stat = StatFs(Environment.getExternalStorageDirectory().getPath())
            stat.blockSize.toLong() * stat.blockCount.toLong()
        }

        Log.d("MainActivity",  "Available bytes: " + availableBytes)
        Log.d("MainActivity", "Available MB: " + availableBytes / 1024 / 1024)

    }


    // From https://stackoverflow.com/a/18858246
    private fun hideSoftKeyBoard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    // From https://stackoverflow.com/a/45605249
    private fun vibratePhone(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator)
                .vibrate(
                    VibrationEffect
                        .createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                )
        } else {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(150)
        }
    }
}