package com.example.aop_part2_chapter7

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat


//TODO: Create CutstomView
class RecordButton(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageButton(context, attrs) {

    fun updateIconWithState(state : currentState) {
        when(state) {
            currentState.BEFORE_RECORDING -> {
                background = ResourcesCompat.getDrawable(resources, R.drawable.ic_record, null)
            }
            currentState.ON_RECORDING -> {
                background = ResourcesCompat.getDrawable(resources, R.drawable.ic_stop, null)
            }
            currentState.AFTER_RECORDING -> {
                background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null)
            }
            currentState.ON_PLAYING -> {
                background = ResourcesCompat.getDrawable(resources, R.drawable.ic_stop, null)
            }
        }
    }
}