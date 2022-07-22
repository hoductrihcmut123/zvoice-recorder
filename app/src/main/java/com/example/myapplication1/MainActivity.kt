package com.example.myapplication1

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication1.database.SQLiteHelper
import com.example.myapplication1.database.model.AudioRecordModel
import com.example.myapplication1.databinding.ActivityMainBinding
import com.example.myapplication1.service.RecordingService
import com.example.myapplication1.utils.Timer
import com.example.myapplication1.view.GalleryActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import java.io.*
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

const val REQUEST_CODE = 300

class MainActivity : AppCompatActivity(), Timer.OnTimeTickListener {

    companion object {
        private var permissions =
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private var permissionGrantedAudio = false
        private var permissionGrantedWrite = false

        private lateinit var amplitudes: ArrayList<Float>

        lateinit var binding: ActivityMainBinding

        var id by Delegates.notNull<Int>()
        var dirPath = ""
        var filename = ""
        var isRecording = false
        var isPaused = false

        lateinit var timer: Timer

        private lateinit var vibrator: Vibrator

        private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

        lateinit var sqLiteHelper: SQLiteHelper

        var recordingService: RecordingService? = null
        lateinit var intent: Intent
    }

    private var duration = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionGrantedAudio = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        permissionGrantedWrite = ActivityCompat.checkSelfPermission(
            this,
            permissions[1]
        ) == PackageManager.PERMISSION_GRANTED

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (!(permissionGrantedAudio and permissionGrantedWrite)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        val bottomSheet = findViewById<LinearLayout>(R.id.bottomSheet)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isDraggable = false

        sqLiteHelper = SQLiteHelper(this)
        GlobalScope.launch(Dispatchers.Default) {
            updateDurationField()
        }

        binding.btnRecord.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecording()
            }

            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        }

        binding.btnList.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        binding.btnSwitch.setOnClickListener{
            if(binding.btnSwitch.isChecked){
                binding.btnCrashTest.visibility = View.VISIBLE
            } else {
                binding.btnCrashTest.visibility = View.GONE
            }
        }

        // button crash test
        binding.btnCrashTest.setOnClickListener {
            val crash = 5 / 0
        }

        binding.btnDelete.isClickable = false

    }

    private fun deleteRecord(id: Int) {
        sqLiteHelper.deleteAudioRecordById(id)
        File("${dirPath}${filename}.mp3").delete()
    }

    private fun renameRecord() {
        val newFilename = binding.bottomSheet.filenameInput.text.toString()

        GlobalScope.launch(Dispatchers.IO) {
            val status = sqLiteHelper.updateAudioRecordNameDuration(
                AudioRecordModel(
                    id = id,
                    filename = newFilename,
                    duration = duration
                )
            )
            withContext(Dispatchers.Main) {
                collapse()
                if (status > -1) {
                    Log.d("renameRecord()", "Rename and update duration successfully")
                } else {
                    Log.d("renameRecord()", "Rename and update duration failed")
                }
            }
        }
    }

    private fun updateDurationField() {
        GlobalScope.launch(Dispatchers.IO) {
            val records: ArrayList<AudioRecordModel> = ArrayList()
            val queryResult = sqLiteHelper.getAllAudioRecord()
            records.addAll(queryResult)

            withContext(Dispatchers.IO) {

                for (i in (0 until records.size)) {

                    if (records[i].duration == "none") {
                        val mediaPlayer = MediaPlayer()
                        mediaPlayer.apply {
                            setDataSource(records[i].filePath)
                            prepare()
                        }

                        sqLiteHelper.updateAudioRecordNameDuration(
                            AudioRecordModel(
                                id = records[i].id,
                                filename = records[i].filename,
                                duration = dateFormat(mediaPlayer.duration)
                            )
                        )
                        mediaPlayer.release()
                    }
                }
            }
        }
    }

    private fun dateFormat(duration: Int): String{
        val d = duration / 1000
        val s = d % 60
        val m = d/60 % 60
        val h = (d - m*60)/ 3600

        val f : NumberFormat = DecimalFormat("00")
        var str = "${f.format(m)}:${f.format(s)}"

        if(h>0) {
            str = "$h:$str"
        }
        return str
    }

    private fun collapse() {
        binding.bottomSheetBG.visibility = View.GONE
        hideKeyboard(binding.bottomSheet.filenameInput)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 200)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            permissionGrantedAudio = grantResults[0] == PackageManager.PERMISSION_GRANTED
            permissionGrantedWrite = grantResults[1] == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun pauseRecording() {

        if (Build.VERSION.SDK_INT >= 24) {
            binding.btnRecord.setImageResource(R.drawable.ic_mic)
            timer.pause()

            GlobalScope.launch(Dispatchers.Default) {
                isPaused = true
                recordingService!!.recorder.pause()
                recordingService!!.showNotification("Recording stopped")
            }
        }
    }

    private fun resumeRecording() {

        if (Build.VERSION.SDK_INT >= 24) {
            binding.btnRecord.setImageResource(R.drawable.ic_pause)
            timer.start()

            GlobalScope.launch(Dispatchers.Default) {
                isPaused = false
                recordingService!!.recorder.resume()
                recordingService!!.showNotification("Recording in progress")
            }
        }
    }

    private fun startRecording() {
        if (!(permissionGrantedAudio and permissionGrantedWrite)) {
            Handler().postDelayed({
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            }, 100)
            return
        }
        // start recordingService here

        intent = Intent(this, RecordingService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
        startService(intent)

        // The recording action is implement in onStartCommand() in RecordingService
    }

    private fun stopRecording() {
        timer.stop()

        recordingService!!.recorder.apply {
            stop()
            release()
        }

        isPaused = false
        isRecording = false

        binding.btnList.visibility = View.VISIBLE
        binding.btnDone.visibility = View.GONE

        binding.btnDelete.isClickable = false
        binding.btnDelete.setImageResource(R.drawable.ic_delete_disabled)

        binding.btnRecord.setImageResource(R.drawable.ic_record)
        binding.btnRecord.setImageResource(R.drawable.ic_mic)

        binding.tvTimer.text = "00:00.00"

        amplitudes = binding.waveformView1.clear()
        binding.waveformView2.clear()
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.text = duration
        this.duration = duration.dropLast(3)
        binding.waveformView1.addAmplitude(recordingService!!.recorder.maxAmplitude.toFloat())
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RecordingService.MyBinder
            recordingService = binder.currentService()

            binding.btnDone.setOnClickListener {
                stopRecording()

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.bottomSheetBG.visibility = View.VISIBLE

                binding.bottomSheet.filenameInput.setText(filename)
            }

            binding.bottomSheet.btnCancel.setOnClickListener {
                deleteRecord(id)
                collapse()
                stopService()
            }

            binding.bottomSheet.btnOk.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO) {
                    renameRecord()
                    stopService()
                    withContext(Dispatchers.Main) {
                        collapse()
                    }
                }
            }

            binding.bottomSheetBG.setOnClickListener {
                deleteRecord(id)
                collapse()
                stopService()
            }

            binding.btnDelete.setOnClickListener {
                stopRecording()
                deleteRecord(id)
                stopService()
                Toast.makeText(this@MainActivity, "Record deleted", Toast.LENGTH_SHORT).show()
            }

            recordingService!!.showNotification("Recording in progress")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            recordingService = null
        }
    }

    private fun stopService() {
        recordingService!!.stopForeground(true)
        recordingService = null
        unbindService(connection)
        stopService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        recordingService!!.stopForeground(true)
        recordingService = null
        unbindService(connection)
        stopService(intent)
    }

}