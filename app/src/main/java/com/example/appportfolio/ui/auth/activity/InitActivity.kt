package com.example.appportfolio.ui.auth.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.appportfolio.*
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.SignManager
import com.example.appportfolio.auth.Status
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.ActivityInitBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.NetworkConnection
import com.example.appportfolio.ui.main.activity.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class InitActivity: AppCompatActivity() {
    var fcmToken:String=""
    lateinit var binding: ActivityInitBinding
    private val viewModel: AuthViewModel by viewModels()
    @Inject
    lateinit var signManager: SignManager
    lateinit var api: AuthApi
    @Inject
    lateinit var userPreferences: UserPreferences
    private var isConnected=false
    lateinit var applyResult:(Status, String?, String?)->Unit
    var token:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window?.apply {
            this.statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        }
        val connection = NetworkConnection(this)
        connection.observe(this) { isconnected ->
           isConnected=isconnected
        }


        binding= DataBindingUtil.setContentView(this,R.layout.activity_init)
        getFirebaseToken()
        viewModel.setAccessToken(runBlocking { userPreferences.authToken.first() })
        subscribeToObserver()
    }
    private fun loginCheck(){
        binding.initprogressbar.visibility=View.VISIBLE
        applyResult={ status: Status, resultdata1: String?, resultdata2: String? ->
            if(status== Status.NOTFOUND) {//sns로그인이 안되어있는경우
                viewModel.autologin(api)//이메일로 자동로그인시도
            }
            else if(status== Status.ERROR)
            {
                Intent(this, AuthActivity::class.java).apply{
                    startActivity(this)
                    finish()
                }
            }
            else
            {
                viewModel.autologin(api)
            }
        }
        signManager.getCurAccountInfo(applyResult)
    }
    private fun subscribeToObserver() {

        viewModel.fcmToken.observe(this){
            fcmToken=it
        }
        viewModel.accessToken.observe(this) { accesstoken ->
            token = accesstoken
            api = RemoteDataSource().buildApi(AuthApi::class.java, token)
            loginCheck()
        }
        viewModel.autologinResponse.observe(this, Event.EventObserver(
            onError = {
                var message:String
                if(!isConnected)
                   message="네트워크 연결상태를 확인 후 다시 시도해주세요"
                else
                    message=it
                binding.initprogressbar.visibility=View.GONE
                Snackbar.make(binding.root,message,Snackbar.LENGTH_INDEFINITE).apply {
                    setAction("다시시도"){
                        viewModel.setAccessToken(runBlocking { userPreferences.authToken.first() })
                    }
                    show()
                }
            }
        ) {
            binding.initprogressbar.visibility=View.GONE
            handleResponse(this,it.resultCode){
                if (it.resultCode == 200) {//프로필 완료된거
                    val intent = Intent(
                        this,
                        MainActivity::class.java
                    ).apply {
                        startActivity(this)
                        finish()
                    }
                }
                else if(it.resultCode==401){
                    val intent = Intent(
                        this,
                        AuthActivity::class.java
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(this)
                    }
                }
                else
                {
                    val intent = Intent(
                        this,
                        AuthCompleteActivity::class.java
                    ).apply {
                        startActivity(this)
                        finish()
                    }
                }
            }
        })
    }
    private fun getFirebaseToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fcmToken=task.result!!
                viewModel.setfcmtoken(task.result!!)
            }
        }
    }
}