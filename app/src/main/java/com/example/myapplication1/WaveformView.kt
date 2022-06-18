package com.example.myapplication1

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6f
    private var width = 6f
    private var screenWidth = 0f
    private var screenHeight = 800f
    private var distance = 5f
    private var maxSpikes = 0

    init {
        paint.color = Color.rgb(190,190,190)

        screenWidth = (resources.displayMetrics.widthPixels * 2/3).toFloat()

        maxSpikes = (screenWidth / (width + distance)).toInt()
    }

    fun addAmplitude(amp: Float){
        var normal = (amp/24).coerceAtMost(screenHeight)     // handle when the value of Amplitude > screen Height
        amplitudes.add(normal)

        spikes.clear()

        var amps = amplitudes.takeLast(maxSpikes).reversed()      // get the last element maxSpikes to make the animation run
        for(i in amps.indices){
            var left = screenWidth - i*(width + distance)
            var top = screenHeight/2 - (amps[i] + 20)/2
            var right = left + width
            var bottom = top + amps[i] + 20
            spikes.add(RectF(left, top, right, bottom))
        }

        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        spikes.forEach{
            canvas?.drawRoundRect(it, radius, radius, paint)
        }
    }
}