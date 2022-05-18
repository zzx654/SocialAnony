package com.example.appportfolio.ui.auth.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.appportfolio.R

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

    }
}