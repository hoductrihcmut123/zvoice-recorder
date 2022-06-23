package com.example.myapplication1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication1.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.launch

const val REQUEST_CODE = 300

class MainActivity : AppCompatActivity(), Timer.OnTimeTickListener {
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private lateinit var amplitudes: ArrayList<Float>

    private lateinit var binding: ActivityMainBinding

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false
    private var isPaused = false
    private var duration = ""

    private lateinit var timer: Timer

    private lateinit var vibrator: Vibrator

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var sqLiteHelper: SQLiteHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        var bottomSheet = findViewById<LinearLayout>(R.id.bottomSheet)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        sqLiteHelper = SQLiteHelper(this)

        binding.btnRecord.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecording()
            }

            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        binding.btnList.setOnClickListener {
            //TODO
            Toast.makeText(this, "List button", Toast.LENGTH_SHORT).show()
        }

        binding.btnDone.setOnClickListener {
            stopRecording()
            Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetBG.visibility = View.VISIBLE

            binding.bottomSheet.filenameInput.setText(filename)
        }

        binding.bottomSheet.btnCancel.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            collapse()
        }

        binding.bottomSheet.btnOk.setOnClickListener {
            collapse()
            save()
        }

        binding.bottomSheetBG.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            collapse()
        }

        binding.btnDelete.setOnClickListener {
            stopRecording()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.isClickable = false
    }

    private fun save(){
        val newFilename = binding.bottomSheet.filenameInput.text.toString()
        if(newFilename != filename)
        {
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)     // put contents of old file to newFile and remove old file
        }

        var filePath = "$dirPath$newFilename.mp3"
        var timestamp = Date().time.toInt()
        var ampsPath = "$dirPath$newFilename"

        try {
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        }catch (e: IOException){}

        val ar = AudioRecordModel(filename = filename, filePath = filePath, timestamp = timestamp, duration = duration, ampsPath = ampsPath)
        GlobalScope.launch{
            val status = sqLiteHelper.insertAudioRecord(ar)
            if(status > -1){
                Log.e("test", "Audio Record added")
            } else {
                Log.e("test", "Audio Record not saved")
            }
        }
    }

    private fun collapse(){
        binding.bottomSheetBG.visibility = View.GONE
        hideKeyboard(binding.bottomSheet.filenameInput)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        },50)
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

        if (requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRecording() {
        recorder.pause()
        isPaused = true
        binding.btnRecord.setImageResource(R.drawable.ic_mic)

        timer.pause()
    }

    private fun resumeRecording() {
        recorder.resume()
        isPaused = false
        binding.btnRecord.setImageResource(R.drawable.ic_pause)

        timer.start()
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        // start recording here

        recorder = if (Build.VERSION.SDK_INT < 31) {
            MediaRecorder()
        } else {
            MediaRecorder(this)
        }
        dirPath = "${externalCacheDir?.absolutePath}"
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date = simpleDateFormat.format(Date())
        filename = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")

            try {
                prepare()
            } catch (e: IOException) {
            }

            start()
        }
        binding.btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false

        binding.waveformView2.start()

        timer.start()

        binding.btnDelete.isClickable = true
        binding.btnDelete.setImageResource(R.drawable.ic_delete)

        binding.btnDone.visibility = View.VISIBLE
        binding.btnList.visibility = View.GONE
    }

    private fun stopRecording() {
        timer.stop()

        recorder.apply {
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
        binding.waveformView1.addAmplitude(recorder.maxAmplitude.toFloat())
    }
}