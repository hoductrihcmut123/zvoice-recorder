package com.example.myapplication1

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class WaveformViewRight(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint()

    private var radius = 6f
    private var width = 6f
    private var screenWidth = 0f
    private var screenHeight = 800f
    private var distance = 5f
    private var maxSpikes = 0

    init {
        paint.color = Color.rgb(220,220,220)

        screenWidth = (resources.displayMetrics.widthPixels).toFloat()
    }

    fun start(){
        maxSpikes = ((screenWidth / (width + distance))).toInt()
        invalidate()
    }

    fun clear(){
        maxSpikes = 0
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        for(i in (0..maxSpikes)){
            val left = screenWidth - i*(width + distance)
            val top = screenHeight/2 - 30/2
            val right = left + width
            val bottom = top + 30
            canvas?.drawRoundRect(RectF(left, top, right, bottom), radius, radius, paint)
        }
    }
}