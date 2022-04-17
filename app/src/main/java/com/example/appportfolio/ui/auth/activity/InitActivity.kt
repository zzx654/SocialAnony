package com.example.appportfolio.ui.auth.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.appportfolio.*
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.SignManager
import com.example.appportfolio.auth.Status
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.ActivityInitBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.activity.MainActivity
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
    lateinit var applyResult:(Status, String?, String?)->Unit
    var token:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window?.apply {
            this.statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

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
                Toast.makeText(this,it, Toast.LENGTH_SHORT).show()
            }
        ) {

            binding.initprogressbar.visibility=View.GONE
            if (it.resultcode == 200) {//프로필 완료된거
                val intent = Intent(
                    this,
                    MainActivity::class.java
                ).apply {
                    startActivity(this)
                    finish()
                }
            } else if(it.resultcode==500){
                val intent = Intent(
                    this,
                    AuthActivity::class.java
                ).apply {
                    startActivity(this)
                    finish()
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