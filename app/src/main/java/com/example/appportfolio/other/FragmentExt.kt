package com.example.appportfolio

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.snackbar(text:String,confirm:Boolean?=false,txt:String?=null,action:(()->Unit)?=null):Snackbar{
    val sbarLength=if(confirm!!) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
    return Snackbar.make(
        requireView(),
        text,
        sbarLength

    ).apply {
        if(confirm)
        {
            setAction(txt){
                action?.invoke()
            }
        }
        show()
    }
}