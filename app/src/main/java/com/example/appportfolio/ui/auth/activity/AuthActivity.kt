package com.example.appportfolio.ui.auth.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.other.NetworkConnection
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
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
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