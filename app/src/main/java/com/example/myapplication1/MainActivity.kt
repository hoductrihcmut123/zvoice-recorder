package com.example.myapplication1

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication1.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*

const val REQUEST_CODE = 300

class MainActivity : AppCompatActivity(), Timer.OnTimeTickListener {

    companion object {
        private var permissions =
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private var permissionGrantedAudio = false
        private var permissionGrantedWrite = false

        private lateinit var amplitudes: ArrayList<Float>

        lateinit var binding: ActivityMainBinding

        var dirPath = ""
        var filename = ""
        var isRecording = false
        var isPaused = false

        lateinit var timer: Timer

        private lateinit var vibrator: Vibrator

        private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

        private lateinit var sqLiteHelper: SQLiteHelper

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

        binding.btnRecord.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecording()
            }

            if(Build.VERSION.SDK_INT >= 26) {
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

        binding.btnDelete.isClickable = false

    }

    private fun save(){
        val newFilename = binding.bottomSheet.filenameInput.text.toString()
        if(newFilename != filename)
        {
            val newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)     // put contents of old file to newFile and remove old file
        }

        val filePath = "$dirPath$newFilename.mp3"
        val timestamp = (Date().time/1000).toInt()
        val ampsPath = "$dirPath$newFilename"

        try {
            val fos = FileOutputStream(ampsPath)
            val out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        }catch (e: IOException){}

        val ar = AudioRecordModel(filename = newFilename, filePath = filePath, timestamp = timestamp, duration = duration, ampsPath = ampsPath)
        GlobalScope.launch{
            val status = sqLiteHelper.insertAudioRecord(ar)
            if(status > -1){
                Log.e("test", "Audio Record added")
                //Toast.makeText(this@MainActivity, "Record saved", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("test", "Audio Record not saved")
                //Toast.makeText(this@MainActivity, "Record save failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun collapse(){
        binding.bottomSheetBG.visibility = View.GONE
        hideKeyboard(binding.bottomSheet.filenameInput)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        },200)
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

        binding.btnRecord.setImageResource(R.drawable.ic_mic)
        timer.pause()

        GlobalScope.launch(Dispatchers.Default) {
            isPaused = true
            recordingService!!.recorder.pause()
            recordingService!!.showNotification("Recording stopped")
        }

    }

    private fun resumeRecording() {

        binding.btnRecord.setImageResource(R.drawable.ic_pause)
        timer.start()

        GlobalScope.launch(Dispatchers.Default) {
            isPaused = false
            recordingService!!.recorder.resume()
            recordingService!!.showNotification("Recording in progress")
        }
    }

    private fun startRecording() {
        if (!(permissionGrantedAudio and permissionGrantedWrite)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        // start recordingService here

        intent = Intent(this, RecordingService::class.java)
        bindService(intent,connection, BIND_AUTO_CREATE)
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
                File("$dirPath$filename.mp3").delete()
                collapse()
                stopService()
            }

            binding.bottomSheet.btnOk.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO) {
                    save()
                    stopService()
                    withContext(Dispatchers.Main) {
                        collapse()
                    }
                }
            }

            binding.bottomSheetBG.setOnClickListener {
                File("$dirPath$filename.mp3").delete()
                collapse()
                stopService()
            }

            binding.btnDelete.setOnClickListener {
                stopRecording()
                File("$dirPath$filename.mp3").delete()
                stopService()
                Toast.makeText(this@MainActivity, "Record deleted", Toast.LENGTH_SHORT).show()
            }

            recordingService!!.showNotification("Recording in progress")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            recordingService = null
        }
    }

    private fun stopService(){
        recordingService!!.stopForeground(true)
        recordingService = null
        unbindService(connection)
        stopService(intent)
    }

}