package com.chronokeep.registration.util

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.chronokeep.registration.R

class Util {
    companion object {
        fun slideDown(ctx: Context?, v: View) {
            val a: Animation = AnimationUtils.loadAnimation(ctx, R.anim.slide_down)
            a.reset()
            v.clearAnimation()
            v.startAnimation(a)
        }

        fun slideUp(ctx: Context?, v: View) {
            val a: Animation = AnimationUtils.loadAnimation(ctx, R.anim.slide_up)
            a.reset()
            v.clearAnimation()
            v.startAnimation(a)
        }
    }
}