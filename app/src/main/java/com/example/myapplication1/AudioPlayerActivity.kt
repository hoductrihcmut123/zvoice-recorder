package com.example.myapplication1

import android.annotation.SuppressLint
import android.content.*
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myapplication1.databinding.ActivityAudioPlayerBinding
import java.text.DecimalFormat
import java.text.NumberFormat


class AudioPlayerActivity : AppCompatActivity(), ServiceConnection {

    private var jumpValue = 5000
    private var playbackSpeed = 1f
    private lateinit var filePath: String

    companion object{
        var delay = 50L
        var isPlaying: Boolean = false
        lateinit var runnable: Runnable
        lateinit var handler: Handler
        var audioPlayerService: AudioPlayerService? = null
        lateinit var filename: String
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityAudioPlayerBinding
        lateinit var intent: Intent
    }

    private var receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("test", "received")
            onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filePath = intent.getStringExtra("filePath").toString()
        filename = intent.getStringExtra("filename").toString()

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("exit $filename.action"))

        // Starting Service
        intent = Intent(this, AudioPlayerService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
        startService(intent)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        binding.tvFilename.text = filename
    }

    private fun playPausePlayer(){
        isPlaying = audioPlayerService!!.mediaPlayer!!.isPlaying
        if(!isPlaying)
        {
            isPlaying = true
            audioPlayerService!!.mediaPlayer!!.start()
            binding.btnPlay.setImageResource(R.drawable.ic_pause_circle)
            audioPlayerService!!.showNotification(R.drawable.ic_pausebig)
            handler.postDelayed(runnable, delay)
        }else {
            isPlaying = false
            audioPlayerService!!.mediaPlayer!!.pause()
            binding.btnPlay.setImageResource(R.drawable.ic_play_circle)
            audioPlayerService!!.showNotification(R.drawable.ic_playbig)
            handler.removeCallbacks(runnable)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        audioPlayerService!!.mediaPlayer!!.stop()
        audioPlayerService!!.mediaPlayer!!.release()
        handler.removeCallbacks(runnable)
        audioPlayerService!!.stopForeground(true)
        audioPlayerService = null
        intent = Intent(this, AudioPlayerService::class.java)
        stopService(intent)
    }

    private fun dateFormat(duration: Int): String{
        val d = duration / 1000
        val s = d % 60
        val m = d/60 % 60
        val h = (d - m*60)/ 3600

        val f : NumberFormat = DecimalFormat("00")
        var str = "$m:${f.format(s)}"

        if(h>0) {
            str = "$h:$str"
        }
        return str
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as AudioPlayerService.MyBinder
        audioPlayerService = binder.currentService()

        // Create MediaPlayer
        audioPlayerService!!.mediaPlayer = MediaPlayer()
        audioPlayerService!!.mediaPlayer?.apply {
            setDataSource(filePath)
            prepare()
        }

        binding.tvTrackDuration.text = dateFormat(audioPlayerService!!.mediaPlayer!!.duration)

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable{
            binding.seekBar.progress = audioPlayerService!!.mediaPlayer!!.currentPosition
            binding.tvTrackProgress.text = dateFormat(audioPlayerService!!.mediaPlayer!!.currentPosition)
            handler.postDelayed(runnable, delay)
        }


        playPausePlayer()

        binding.seekBar.max = audioPlayerService!!.mediaPlayer!!.duration

        audioPlayerService!!.mediaPlayer!!.setOnCompletionListener {
            binding.btnPlay.setImageResource(R.drawable.ic_play_circle)
            audioPlayerService!!.showNotification(R.drawable.ic_playbig)
            handler.removeCallbacks(runnable, delay)
        }

        binding.btnPlay.setOnClickListener{
            playPausePlayer()
        }

        binding.btnForward.setOnClickListener{
            audioPlayerService!!.mediaPlayer!!.seekTo(audioPlayerService!!.mediaPlayer!!.currentPosition + jumpValue)
            binding.seekBar.progress += jumpValue
        }

        binding.btnBackward.setOnClickListener{
            audioPlayerService!!.mediaPlayer!!.seekTo(audioPlayerService!!.mediaPlayer!!.currentPosition - jumpValue)
            binding.seekBar.progress -= jumpValue
        }

        binding.chip.setOnClickListener{
            if (playbackSpeed != 2f) {
                playbackSpeed += 0.5f
            }
            else {
                playbackSpeed = 0.5f
            }

            audioPlayerService!!.mediaPlayer!!.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
            binding.chip.text = "x $playbackSpeed"
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioPlayerService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })


        audioPlayerService!!.showNotification(R.drawable.ic_pausebig)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        audioPlayerService = null
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

}