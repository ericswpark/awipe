package android.com.ericswpark.awipe

import android.content.Context
import android.os.*
import android.os.storage.StorageManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startWipeClicked(v: View) {
        confirmWipe(v)
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
        wipeProgressBar.visibility = View.VISIBLE
        wipeProgressText.visibility = View.VISIBLE

        Snackbar.make(v, R.string.main_activity_wipe_started, Snackbar.LENGTH_SHORT).show()
        vibratePhone(v.context)

        thread {
            wipe(v)

            Snackbar.make(v, R.string.main_activity_wipe_finished, Snackbar.LENGTH_SHORT).show()
            vibratePhone(v.context)

            // On end, enable button
            runOnUiThread {
                startButton.isEnabled = true
            }
        }
    }

    private fun wipe(v: View) {
        // Query free space
        val availableBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val storageManager = applicationContext.getSystemService(
                StorageManager::class.java
            )
            val appSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(filesDir)
            storageManager.getAllocatableBytes(appSpecificInternalDirUuid)
        } else {
            val stat = StatFs(Environment.getExternalStorageDirectory().getPath())
            stat.blockSize.toLong() * stat.blockCount.toLong()
        }

        Log.d("MainActivity", "Available bytes: $availableBytes")
        Log.d("MainActivity", "Available MB: " + availableBytes / 1024 / 1024)

        val file = File(applicationContext.filesDir, "wipeFile")
        file.createNewFile()

        if (file.exists()) {
            val fo = FileOutputStream(file, true)

            var byteCount: Long = 0

            while (true) {
                try {
                    val randomBytes = Random.nextBytes(1024 * 1024)
                    fo.write(randomBytes)
                    fo.flush()
                    byteCount += 1024 * 1024
                    runOnUiThread {
                        updateWipeProgress(byteCount, availableBytes, v)
                    }
                } catch (e: Exception) {
                    // We ran out of space!
                    break
                }
            }

            fo.close()
        }

        // Delete file for next run
        file.delete()
    }

    private fun updateWipeProgress(currentBytes: Long, totalBytes: Long, v: View) {
        val progressBar = findViewById<ProgressBar>(R.id.main_activity_wipe_progress_bar)
        val progressText = findViewById<TextView>(R.id.main_activity_wipe_progress_text)

        val percentage: Double = currentBytes.toDouble() / totalBytes * 100
        progressBar.progress = percentage.toInt()

        progressText.text = String.format(
            "%d/%d, %.2f%%",
            currentBytes,
            totalBytes,
            currentBytes.toDouble() / totalBytes * 100
        )
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