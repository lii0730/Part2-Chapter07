package com.example.aop_part2_chapter7

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat

//  TODO: CustomView를 직접 그리는 이유
//        레이아웃을 직접 가져와서 작업을 하면 처리속도가 느림 -> 많은 수의 뷰를 추가하면 속도가 현저하게 느림
//        속도 개선을 위한 목적도 있음

class AudioRecordView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    companion object {
        //TODO: 상수 정의
        private const val LINE_WIDTH = 10f // 그려질 선의 두께
        private const val LINE_SPACE = 15f // 그려질 선 사이의 간격
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat() // 그려질 선의 최대 높이
        private const val ACTION_INTERVAL = 100L // Runnable 동작 간격
    }

    //TODO: Nullable한 Int로 형변환
    var onRequestCurrentAmplitude: (() -> Int)? = null

    //TODO: 선을 그릴 Paint 생성
    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ResourcesCompat.getColor(resources, R.color.purple_500, null)
        strokeWidth = LINE_WIDTH
        strokeCap = Paint.Cap.ROUND //라인의 끝 지정?
    }

    private var drawingWidth: Int = 0
    private var drawingHeight: Int = 0
    private var drawingAmplitudes: List<Int> = emptyList()
    private var isReplaying: Boolean = false // 재생 할 때 처음으로 돌아가서 재생
    private var replayingPosition: Int = 0

    private val visualizeRepeatAction: Runnable = object : Runnable {
        override fun run() {
            if (!isReplaying) {
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0

                //제일 나중에 들어온 amplitude를 가장 앞쪽에 배치
                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes
            } else {
                replayingPosition++
            }

            invalidate() // onDraw 호출

            handler.postDelayed(this, ACTION_INTERVAL)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return //null일 경우 return

        val centerY = drawingHeight / 2f
        var offsetX = drawingWidth.toFloat() //시작 포인트 가장 오른쪽 = 가로 길이

        drawingAmplitudes.let { amplitudes ->
            if (isReplaying) {
                amplitudes.takeLast(replayingPosition) //replayPosition의 수만큼 뒤에서부터 가져옴
            } else {
                amplitudes
            }
        }
        .forEach { amplitude ->
            val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F

            offsetX -= LINE_SPACE // 오른쪽에서 왼쪽으로 그려진다
            if (offsetX < 0) return@forEach // 왼쪽을 벗어난다면

            canvas.drawLine(
                offsetX,
                centerY - lineLength / 2F,
                offsetX, //위아래로만 그려지기 때문
                centerY + lineLength / 2F,
                amplitudePaint
            )
        }
    }

    fun startVisualizing(isReplaying: Boolean) {
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }

    fun stopVisualizing() {
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction)
    }

    fun clearVisualization() {
        drawingAmplitudes = emptyList()
        invalidate()
    }
}
