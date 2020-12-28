package android.com.ericswpark.awipe

import android.content.Context
import android.os.*
import android.os.storage.StorageManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.random.Random


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

        // Enable progress bars
        val wipeProgressBar = findViewById<ProgressBar>(R.id.main_activity_wipe_progress_bar)
        val wipeProgressText = findViewById<TextView>(R.id.main_activity_wipe_progress_text)
        val multiProgressBar = findViewById<ProgressBar>(R.id.main_activity_run_multiple_progress_bar)
        val multiProgressText = findViewById<TextView>(R.id.main_activity_run_multiple_progress_text)
        wipeProgressBar.visibility = View.VISIBLE
        wipeProgressText.visibility = View.VISIBLE

        Snackbar.make(v, R.string.main_activity_wipe_started, Snackbar.LENGTH_SHORT).show()
        vibratePhone(v.context)

        // Get run count
        val editText = findViewById<EditText>(R.id.main_activity_run_multiple_count_edit_text)
        val checkBox = findViewById<CheckBox>(R.id.main_activity_run_multiple_checkbox)

        if (checkBox.isChecked) {
            // Multiple runs
            val count = Integer.parseInt(editText.text.toString())

            // Enable progress bars
            multiProgressBar.visibility = View.VISIBLE
            multiProgressText.visibility = View.VISIBLE

            for (i in 1..count) {
                updateRunProgress(i, count, v)
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

        Log.d("MainActivity", "Available bytes: " + availableBytes)
        Log.d("MainActivity", "Available MB: " + availableBytes / 1024 / 1024)

        val file = File(applicationContext.filesDir, "wipeFile")
        file.createNewFile()


        var byteCount = 0

        while (true) {
            try {
                writeRandomToFile(file)
                byteCount += 1024 * 1024
                updateWipeProgress(byteCount, availableBytes, v)
            } catch (e: Exception) {
                // We ran out of space!
                break
            }
        }

        // Delete file for next run
        file.delete()
    }

    // Writes 1 MB of random data per invocation
    private fun writeRandomToFile(file: File) {
        if (file.exists()) {
            val fo = FileOutputStream(file, true)
            val randomBytes = Random.nextBytes(1024 * 1024)
            fo.write(randomBytes)
            fo.close()
        }
    }

    private fun updateWipeProgress(currentBytes: Int, totalBytes: Long, v: View) {
        val progressBar = findViewById<ProgressBar>(R.id.main_activity_wipe_progress_bar)
        val progressText = findViewById<TextView>(R.id.main_activity_wipe_progress_text)

        val percentage = currentBytes * 100 / totalBytes
        progressBar.progress = percentage.toInt()

        progressText.text = String.format("%d/%d", currentBytes, totalBytes)
    }

    private fun updateRunProgress(currentRun: Int, totalRuns: Int, v: View) {
        val progressBar = findViewById<ProgressBar>(R.id.main_activity_run_multiple_progress_bar)
        val progressText = findViewById<TextView>(R.id.main_activity_run_multiple_progress_text)

        val percentage = currentRun * 100 / totalRuns
        progressBar.progress = percentage

        progressText.text = String.format("%d/%d", currentRun, totalRuns)
    }


    // From https://stackoverflow.com/a/18858246
    private fun hideSoftKeyBoard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) { // verify if the soft keyboard is open
            try {
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            } catch (e: NullPointerException) {
                // Ignore, as the keyboard is not open
            }
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