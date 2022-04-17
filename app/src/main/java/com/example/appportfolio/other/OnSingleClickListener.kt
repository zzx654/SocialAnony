package com.example.appportfolio.other

import android.content.ContentValues.TAG
import android.util.Log
import android.view.View

class OnSingleClickListener(  private val clickListener: View.OnClickListener,
                              private val interval: Long = 300
) :
    View.OnClickListener {

    private var clickable = true

    override fun onClick(v: View?) {
        if (clickable) {
            clickable = false
            v?.run {
                postDelayed({
                    clickable = true
                }, interval)
                clickListener.onClick(v)
            }
        } else {
            Log.d(TAG, "waiting for a while")
        }
    }
}