package com.example.appportfolio.other

import android.graphics.Rect
import android.view.View

class Keyboard {

    private var isKeyboardShowing: Boolean = false // 현재 키보드 보이는지 여부

    var keyboardHeight: Int = -1 // 키보드 높이



    // 키보드 높이 구하기

    private fun getKeyboardHeight(targetView: View) {


        val rectangle = Rect()

        targetView.getWindowVisibleDisplayFrame(rectangle)

        val screenHeight: Int = targetView.rootView.height // 현재 뷰(activity, fragment)의 전체 높이

        val tmpKeyboardSize: Int = screenHeight - rectangle.height() // 예상 키보드 높이



        // 뷰 높이의 10%가 예상 키보드 높이보다 낮을 경우 키보드 올라와있다고 판단

        if (tmpKeyboardSize > screenHeight * 0.1) {

            keyboardHeight = tmpKeyboardSize // 키보드 높이 세팅

            isKeyboardShowing = true

        } else {

            isKeyboardShowing = false

        }

    }



    // 키보드 현재 보이는지 여부

    fun isShowing(targetView: View): Boolean {

        getKeyboardHeight(targetView)

        return isKeyboardShowing

    }

}