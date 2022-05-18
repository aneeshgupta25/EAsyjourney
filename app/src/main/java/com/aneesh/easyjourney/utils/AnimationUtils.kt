package com.aneesh.easyjourney.utils

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator

object AnimationUtils {

    fun polyLineAnimator() : ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(0,100)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 2000
        return valueAnimator
    }

    fun carAnimator() : ValueAnimator{
        //Here, we took 0f and 1f coz we want to move the cab very slowly between the 2 given points
        // ( i.e. taking float of such small range will give lot of small values)
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 3000
        return valueAnimator
    }

}