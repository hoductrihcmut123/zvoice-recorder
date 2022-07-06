package com.example.myapplication1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

class AudioPlayerNotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PLAY -> if(AudioPlayerActivity.isPlaying) pauseAudio() else playAudio()
            ApplicationClass.EXIT -> {
                AudioPlayerActivity.audioPlayerService!!.stopForeground(true)
                AudioPlayerActivity.audioPlayerService = null
                exitProcess(1)
            }
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
}