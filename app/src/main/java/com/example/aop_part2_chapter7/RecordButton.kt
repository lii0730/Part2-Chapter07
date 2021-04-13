package com.example.aop_part2_chapter7

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton


//TODO: Create CutstomView
class RecordButton(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageButton(context, attrs) {

    fun updateIconWithState(state : currentState) {
        when(state) {
            currentState.BEFORE_RECORDING -> {
                setImageResource(R.drawable.ic_record)
            }
            currentState.ON_RECORDING -> {
                setImageResource(R.drawable.ic_stop)
            }
            currentState.AFTER_RECORDING -> {
                setImageResource(R.drawable.ic_play)
            }
            currentState.ON_PLAYING -> {
                setImageResource(R.drawable.ic_stop)
            }
        }
    }
}