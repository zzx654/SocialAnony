package com.example.appportfolio.ui.auth.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.R
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.responses.*
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository,
                                        private val dispatcher:CoroutineDispatcher= Dispatchers.Main,
                                        private val applicationContext: Context):
    ViewModel() {
    private lateinit var a: Job
    private val _withdrawalResponse = MutableLiveData<Event<Resource<intResponse>>>()
    val withdrawalResponse: LiveData<Event<Resource<intResponse>>> = _withdrawalResponse
    private val _getUseridResponse = MutableLiveData<Event<Resource<getuserResponse>>>()
    val getUseridResponse: LiveData<Event<Resource<getuserResponse>>> = _getUseridResponse

    private val _getmyprofileResponse = MutableLiveData<Event<Resource<getprofileResponse>>>()
    val getmyprofileResponse: LiveData<Event<Resource<getprofileResponse>>> = _getmyprofileResponse
    private val _toggleChatResponse = MutableLiveData<Event<Resource<intResponse>>>()
    val toggleChatResponse: LiveData<Event<Resource<intResponse>>> = _toggleChatResponse
    private val _getChatonoffResponse = MutableLiveData<Event<Resource<intResponse>>>()
    val getChatonoffResponse: LiveData<Event<Resource<intResponse>>> = _getChatonoffResponse

    private val _findpasswordResponse = MutableLiveData<Event<Resource<intResponse>>>()
    val findpasswordResponse: LiveData<Event<Resource<intResponse>>> = _findpasswordResponse
    private val _changepasswordResponse = MutableLiveData<Event<Resource<intResponse>>>()
    val changepasswordResponse: LiveData<Event<Resource<intResponse>>> = _changepasswordResponse

    private val _checkfcmresponse = MutableLiveData<Event<Resource<intResponse>>>()
    val checkfcmresponse: LiveData<Event<Resource<intResponse>>> = _checkfcmresponse


    private val _timerString = MutableLiveData<String>()
    val timerString: LiveData<String> = _timerString

    private val _curBirth = MutableLiveData<String>()
    val curBirth: LiveData<String> = _curBirth

    private val _platform = MutableLiveData<String>()
    val platform: LiveData<String> = _platform

    private val _userid = MutableLiveData<Int>()
    val userid: LiveData<Int> = _userid

    private val _timerCount = MutableLiveData<Int>()

    private val _verifyResponse = MutableLiveData<Event<Resource<VerifyResponse>>>()
    val verifyResponse: LiveData<Event<Resource<VerifyResponse>>> = _verifyResponse


    private val _SocialSignResponse = MutableLiveData<Event<Resource<LoginResponse>>>()
    val SocialSignResponse: LiveData<Event<Resource<LoginResponse>>> = _SocialSignResponse

    private val _authCompleteResponse = MutableLiveData<Event<Resource<String>>>()
    val authCompleteResponse: LiveData<Event<Resource<String>>> = _authCompleteResponse

    private val _registerResponse = MutableLiveData<Event<Resource<intResponse>>>()
    val registerResponse: LiveData<Event<Resource<intResponse>>> = _registerResponse

    private val _loginResponse = MutableLiveData<Event<Resource<LoginResponse>>>()
    val loginResponse: LiveData<Event<Resource<LoginResponse>>> = _loginResponse

    private val _fcmToken = MutableLiveData<String>()
    val fcmToken: LiveData<String> = _fcmToken

    private val _accessToken = MutableLiveData<String?>()
    val accessToken: LiveData<String?> = _accessToken


    private val _autologinResponse = MutableLiveData<Event<Resource<checkProfileResponse>>>()
    val autologinResponse: LiveData<Event<Resource<checkProfileResponse>>> = _autologinResponse

    private val _logoutResponse = MutableLiveData<Event<Resource<LogoutResponse>>>()
    val logoutResponse: LiveData<Event<Resource<LogoutResponse>>> = _logoutResponse

    private val _checkProfileResponse = MutableLiveData<Event<Resource<checkProfileResponse>>>()
    val checkProfileResponse: LiveData<Event<Resource<checkProfileResponse>>> =
        _checkProfileResponse

    fun setPlatform(platform:String){
        _platform.postValue(platform)
    }
    fun setCurBirth(birth: String) {
        _curBirth.postValue(birth)
    }
    fun timerStart() {
        if (::a.isInitialized) a.cancel()
        _timerCount.postValue(180)
        timertoString(180)
        a = viewModelScope.launch(dispatcher) {
            while (_timerCount.value!! > 0) {
                delay(1000L)
                _timerCount.postValue(_timerCount.value!!.minus(1))
                timertoString(_timerCount.value!!)
            }
        }
    }
    private fun timertoString(timer: Int) {
        val m: String = (timer / 60).toString()
        val s: String
        val sec = (timer % 60)
        s = if (sec < 10)
            "0$sec"
        else
            sec.toString()

        _timerString.postValue("$m:$s")
    }
    fun timerStop() {
        if (::a.isInitialized) a.cancel()
    }


    fun setAccessToken(accessToken: String?) {
        _accessToken.postValue(accessToken)

    }


    fun setfcmtoken(token: String) {
        _fcmToken.postValue(token)
    }
    fun setUserid(userid:Int)
    {
        _userid.postValue(userid)
    }
    fun autologin(api: AuthApi) {
        _autologinResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.autologin(api)
            _autologinResponse.postValue(Event(result))
        }
    }
    fun getUserid(api: AuthApi) {
        _getUseridResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.getUserid(api)
            _getUseridResponse.postValue(Event(result))
        }
    }
    fun withdrawal(api:AuthApi){
        _withdrawalResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.withdrawal(api)
            _withdrawalResponse.postValue(Event(result))
        }
    }
    fun findpassword(email: String,api:AuthApi) {
        _findpasswordResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.findpassword(email, api)
            _findpasswordResponse.postValue(Event(result))
        }
    }
    fun changepassword(curpw:String,newpw:String,api:AuthApi) {
        _changepasswordResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.changepassword(curpw,newpw, api)
            _changepasswordResponse.postValue(Event(result))
        }
    }
    fun checkProfile( api: AuthApi) {
        _checkProfileResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.checkProfile(api)
            _checkProfileResponse.postValue(Event(result))
        }
    }
    fun getChatonoff(api:AuthApi)
    {
        _getChatonoffResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.getChatonoff( api)
            _getChatonoffResponse.postValue(Event(result))
        }
    }
    fun togglechat(toggle:Int,api:AuthApi)
    {
        _toggleChatResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.togglechat(toggle, api)
            _toggleChatResponse.postValue(Event(result))
        }
    }
    fun requestVerify(
        phone:String,
        api:AuthApi
    )
    {
        _verifyResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = authRepository.requestVerify(phone,api)
            _verifyResponse.postValue(Event(result))
        }

    }
    fun login(
        email: String,
        password: String,
        fcmToken: String,
        api: AuthApi
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            val error = applicationContext.getString(R.string.error_input_empty)
            _loginResponse.postValue(Event(Resource.Error(error)))
            return
        }
        _loginResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.login(email, password, fcmToken, api)
            _loginResponse.postValue(Event(result))
        }
    }
    fun register(
        email: String,
        password: String,
        code:String,
        phone:String,
        api: AuthApi
    ) {
        _registerResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.register(email, password,code,phone, api)
            _registerResponse.postValue(Event(result))
        }
    }
    fun getmyprofile(
        api: AuthApi
    ) {
        _getmyprofileResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.getmyprofile(api)
            _getmyprofileResponse.postValue(Event(result))
        }
    }
    fun AuthComplete(
        profileimage:String?,
        nickname: String,
        gender: String,
        age: String,
        api: AuthApi
    ) {
        _authCompleteResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.AuthComplete(profileimage,nickname, gender, age, api)
            _authCompleteResponse.postValue(Event(result))
        }
    }
    fun signWithSocial(
        platform: String,
        account: String,
        fcmtoken: String,
        api: AuthApi
    ) {
        _SocialSignResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.signWithSocial(platform, account, fcmtoken, api)
            _SocialSignResponse.postValue(Event(result))
        }
    }
    fun logout(api: AuthApi) {
        _logoutResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.logout(api)
            _logoutResponse.postValue(Event(result))
        }
    }


    fun checkfcmtoken(fcmtoken: String,api: AuthApi)
    {
        _checkfcmresponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result=authRepository.checkfcmtoken(fcmtoken, api)
            _checkfcmresponse.postValue(Event(result))
        }
    }
}