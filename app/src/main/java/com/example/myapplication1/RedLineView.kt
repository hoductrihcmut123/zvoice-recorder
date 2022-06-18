package com.example.myapplication1

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class RedLineView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var paintLine = Paint()
    private var path = Path()

    init {
        paintLine.apply {
            color = Color.parseColor("#F4511E")
            strokeWidth = 6f
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        path.moveTo(0f, 0f)
        path.lineTo(20f, 0f)
        path.lineTo(10f, 20f)
        canvas?.drawPath(path, paintLine)

        path.moveTo(0f, 830f)
        path.lineTo(20f, 830f)
        path.lineTo(10f, 810f)
        canvas?.drawPath(path, paintLine)

        canvas?.drawLine(10f, 10f, 10f, 830f -10f, paintLine)
    }
}