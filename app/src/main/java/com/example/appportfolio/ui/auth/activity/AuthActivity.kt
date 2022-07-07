package com.example.appportfolio.ui.auth.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.other.Constants
import com.example.appportfolio.other.NetworkConnection
import com.example.appportfolio.ui.main.fragments.HomeFragment
import com.google.firebase.messaging.FirebaseMessaging

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity:AppCompatActivity() {
    private lateinit var viewModel: AuthViewModel
    var isConnected:Boolean?=null
    var fcmToken:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        val connection = NetworkConnection(this)
        connection.observe(this) { isconnected ->
            isConnected=isconnected
        }
        getFirebaseToken()

    }
    private fun getFirebaseToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                viewModel.setfcmtoken(task.result!!)
            }
        }
        viewModel.fcmToken.observe(this){
            fcmToken=it
        }
    }
}