package com.example.aop_part2_chapter7

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class CustomTimeTextView(
    context : Context,
    attrs: AttributeSet
) : AppCompatTextView(context, attrs) {

    private var startTimeStamp : Long = 0L
    private val countUpAction : Runnable = object : Runnable {
        override fun run() {
            val currentTimeStamp = SystemClock.elapsedRealtime()

           val countTimeSeconds = ((currentTimeStamp - startTimeStamp)/1000L).toInt()
            updateCountTime(countTimeSeconds)

            handler?.postDelayed(this, 1000L)
        }
    }

    fun startCountUp() {
        startTimeStamp = SystemClock.elapsedRealtime()
        handler?.post(countUpAction)
    }

    fun stopCountUp() {
        handler?.removeCallbacks(countUpAction)
    }

    private fun updateCountTime(countTimeSec : Int) {
        val min = countTimeSec / 60
        val sec = countTimeSec % 60

        text ="%02d:%02d".format(min, sec)
    }
}