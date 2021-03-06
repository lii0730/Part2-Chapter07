package com.example.aop_part2_chapter7

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock


abstract class CountUpTimer(val interval: Long) {
    private var currentStampTime: Long = 0

    private val handler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val elapsedTime = SystemClock.elapsedRealtime() - currentStampTime
            onTick(elapsedTime)
            sendMessageDelayed(obtainMessage(MSG), interval)
        }
    }

    fun start() {
        currentStampTime = SystemClock.elapsedRealtime()
        handler.sendMessage(handler.obtainMessage(MSG))
    }

    fun stop() {
        handler.removeMessages(MSG)
    }

    fun reset() {
        synchronized(this) { currentStampTime = SystemClock.elapsedRealtime() }
    }

    abstract fun onTick(elapsedTime: Long)

    companion object {
        private const val MSG = 1
    }
}
