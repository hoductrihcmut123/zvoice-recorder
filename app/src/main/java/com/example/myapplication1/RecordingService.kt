package com.example.myapplication1

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.View
import androidx.core.app.NotificationCompat
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecordingService : Service() {
    private var myBinder = MyBinder()
    lateinit var recorder: MediaRecorder

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    inner class MyBinder: Binder(){
        fun currentService(): RecordingService{
            return this@RecordingService
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        recorder = if (Build.VERSION.SDK_INT < 31) {
            MediaRecorder()
        } else {
            MediaRecorder(this)
        }

        MainActivity.dirPath = "${getExternalFilesDir(null)?.absolutePath}/"
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd_hh.mm.ss")
        val date = simpleDateFormat.format(Date())
        MainActivity.filename = "audioRecord_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("${MainActivity.dirPath}${MainActivity.filename}.mp3")

            try {
                prepare()
            } catch (e: IOException) {
            }

            start()
        }
        MainActivity.binding.btnRecord.setImageResource(R.drawable.ic_pause)
        MainActivity.isRecording = true
        MainActivity.isPaused = false

        MainActivity.binding.waveformView2.start()

        MainActivity.timer.start()

        MainActivity.binding.btnDelete.isClickable = true
        MainActivity.binding.btnDelete.setImageResource(R.drawable.ic_delete)

        MainActivity.binding.btnDone.visibility = View.VISIBLE
        MainActivity.binding.btnList.visibility = View.GONE

        return START_NOT_STICKY
    }

    fun showNotification(contentText: String){

        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentTitle("Record")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_mic)
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.ic_largeiconservice))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .build()

        startForeground(13, notification)
    }

}