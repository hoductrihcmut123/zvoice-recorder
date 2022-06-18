package com.example.myapplication1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.media.MediaRecorder
import android.opengl.Visibility
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.myapplication1.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


const val REQUEST_CODE = 300

class MainActivity : AppCompatActivity(), Timer.OnTimeTickListener {
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private lateinit var amplitudes: ArrayList<Float>

    private lateinit var binding: ActivityMainBinding

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename= ""
    private var isRecording = false
    private var isPaused = false

    private lateinit var timer: Timer

    private lateinit var vibrator: Vibrator

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

        binding.btnRecord.setOnClickListener {
            when{
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecording()
            }

            vibrator.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE))
        }

        binding.btnList.setOnClickListener{
            //TODO
            Toast.makeText(this,"List button", Toast.LENGTH_SHORT).show()
        }

        binding.btnDone.setOnClickListener{
            stopRecording()
            //TODO
            Toast.makeText(this,"Record saved", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.setOnClickListener{
            stopRecording()
            File("$dirPath$filename.mp3")
            Toast.makeText(this,"Record deleted", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.isClickable = false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRecording(){
        recorder.pause()
        isPaused = true
        binding.btnRecord.setImageResource(R.drawable.ic_mic)

        timer.pause()
    }

    private fun resumeRecording(){
        recorder.resume()
        isPaused = false
        binding.btnRecord.setImageResource(R.drawable.ic_pause)

        timer.start()
    }

    private fun startRecording(){
        if(!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        // start recording here

        recorder = if(Build.VERSION.SDK_INT < 31) {
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
            }catch (e: IOException) {}

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

    private fun stopRecording(){
        timer.stop()

        recorder.apply {
            stop()
            release()
        }

        isPaused = false
        isRecording = false

        binding.btnList.visibility = View.VISIBLE
        binding.btnDone.visibility= View.GONE

        binding.btnDelete.isClickable = false
        binding.btnDelete.setImageResource(R.drawable.ic_delete_disabled)

        binding.btnRecord.setImageResource(R.drawable.ic_record)

        binding.tvTimer.text = "00:00.00"

        amplitudes = binding.waveformView1.clear()
        binding.waveformView2.clear()
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.text = duration
        binding.waveformView1.addAmplitude(recorder.maxAmplitude.toFloat())
    }
}