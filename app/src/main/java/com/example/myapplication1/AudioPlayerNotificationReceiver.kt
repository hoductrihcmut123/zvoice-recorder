package com.example.myapplication1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AudioPlayerNotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PLAY -> if(AudioPlayerActivity.isPlaying) pauseAudio() else playAudio()
            ApplicationClass.EXIT -> exit(context)
        }
    }

    private fun playAudio() {
        AudioPlayerActivity.isPlaying = true
        AudioPlayerActivity.audioPlayerService!!.mediaPlayer!!.start()
        AudioPlayerActivity.audioPlayerService!!.showNotification(R.drawable.ic_pausebig)
        AudioPlayerActivity.binding.btnPlay.setImageResource(R.drawable.ic_pause_circle)
        AudioPlayerActivity.handler.postDelayed(AudioPlayerActivity.runnable, AudioPlayerActivity.delay)
    }

    private fun pauseAudio() {
        AudioPlayerActivity.isPlaying = false
        AudioPlayerActivity.audioPlayerService!!.mediaPlayer!!.pause()
        AudioPlayerActivity.audioPlayerService!!.showNotification(R.drawable.ic_playbig)
        AudioPlayerActivity.binding.btnPlay.setImageResource(R.drawable.ic_play_circle)
        AudioPlayerActivity.handler.removeCallbacks(AudioPlayerActivity.runnable)
    }

    private fun exit(context: Context?){

        // Close status bar
        if (Build.VERSION.SDK_INT < 31){
            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context!!.sendBroadcast(it)
        }

        val local = Intent()
        local.action = "exit ${AudioPlayerActivity.filename}.action"
        if (context != null) {
            Log.e("test", "check sendBroadCast ${AudioPlayerActivity.filename}")
            LocalBroadcastManager.getInstance(context).sendBroadcast(local)
        }
    }
}