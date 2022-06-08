package com.example.appportfolio

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.responses.*
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.concurrent.timer

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
    private val _nicknameResponse = MutableLiveData<Event<Resource<NicknameResponse>>>()
    val nicknameResponse: LiveData<Event<Resource<NicknameResponse>>> = _nicknameResponse
    private val _verified = MutableLiveData<Boolean?>()
    val verified: LiveData<Boolean?> = _verified
    private val _emailChecked = MutableLiveData<Boolean>()
    val emailChecked: LiveData<Boolean> = _emailChecked
    private val _timerString = MutableLiveData<String>()
    val timerString: LiveData<String> = _timerString

    private val _curBirth = MutableLiveData<String>()
    val curBirth: LiveData<String> = _curBirth

    private val _platform = MutableLiveData<String>()
    val platform: LiveData<String> = _platform

    private val _userid = MutableLiveData<Int>()
    val userid: LiveData<Int> = _userid

    private val _timerCount = MutableLiveData<Int>()

    private val _verifyCode = MutableLiveData<String>()
    val verifyCode: LiveData<String> = _verifyCode

    private val _emailVerifyResponse = MutableLiveData<Event<Resource<VerifyResponse>>>()
    val emailVerifyResponse: LiveData<Event<Resource<VerifyResponse>>> = _emailVerifyResponse

    private val _codeResponse = MutableLiveData<Event<Resource<VerifyResponse>>>()
    val codeResponse: LiveData<Event<Resource<VerifyResponse>>> = _codeResponse

    private val _verifyResponse = MutableLiveData<Event<Resource<VerifyResponse>>>()
    val verifyResponse: LiveData<Event<Resource<VerifyResponse>>> = _verifyResponse

    private val _getAccountResponse = MutableLiveData<Event<Resource<accountResponse>>>()
    val getAccountResponse: LiveData<Event<Resource<accountResponse>>> = _getAccountResponse

    private val _SocialSignResponse = MutableLiveData<Event<Resource<LoginResponse>>>()
    val SocialSignResponse: LiveData<Event<Resource<LoginResponse>>> = _SocialSignResponse

    private val _authCompleteResponse = MutableLiveData<Event<Resource<String>>>()
    val authCompleteResponse: LiveData<Event<Resource<String>>> = _authCompleteResponse

    private val _registerResponse = MutableLiveData<Event<Resource<String>>>()
    val registerResponse: LiveData<Event<Resource<String>>> = _registerResponse

    private val _loginResponse = MutableLiveData<Event<Resource<LoginResponse>>>()
    val loginResponse: LiveData<Event<Resource<LoginResponse>>> = _loginResponse

    private val _nicknameChecked = MutableLiveData<Boolean>()
    val nicknameChecked: LiveData<Boolean> = _nicknameChecked
    private val _fcmToken = MutableLiveData<String>()
    val fcmToken: LiveData<String> = _fcmToken

    private val _accessToken = MutableLiveData<String?>()
    val accessToken: LiveData<String?> = _accessToken


    private val _curGender = MutableLiveData<String?>()
    val curGender: LiveData<String?> = _curGender

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
    fun setNicknameChecked(b: Boolean) {
        _nicknameChecked.postValue(b)
    }
    fun setverified(verified: Boolean?) {
        _verified.postValue(verified)
    }
    fun setemailChecked(checked: Boolean?) {
        _emailChecked.postValue(checked!!)
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
    fun timertoString(timer: Int) {
        var m: String
        m = (timer!! / 60).toString()
        var s: String
        var sec = (timer % 60)
        if (sec < 10)
            s = "0" + sec.toString()
        else
            s = sec.toString()

        _timerString.postValue(m + ":" + s)
    }
    fun timerStop() {
        if (::a.isInitialized) a.cancel()
    }
    fun setverifyCode(code: String) {
        _verifyCode.postValue(code)
    }

    fun setCurGender(gender: String) {
        _curGender.postValue(gender)
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

    fun verifycode(
        phone:String,
        code:String,
        api:AuthApi
    )
    {
        _codeResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result=authRepository.verifycode(phone,code,api)
            _codeResponse.postValue(Event(result))
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
        api: AuthApi
    ) {
        _registerResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.register(email, password, api)
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
        nickname: String,
        gender: String,
        age: String,
        api: AuthApi
    ) {
        val error = if (nickname.isEmpty() || gender.isEmpty() || age.isEmpty()) {
            applicationContext.getString(R.string.error_input_empty)
        } else null

        error?.let {
            _authCompleteResponse.postValue(Event(Resource.Error(it)))
            return
        }
        _authCompleteResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.AuthComplete(nickname, gender, age, api)
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
    fun getcurAccountInfo(api: AuthApi) {
        _getAccountResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.getcurAccountInfo(api)
            _getAccountResponse.postValue(Event(result))
        }
    }
    fun requestEmail(email: String, api: AuthApi) {
        _emailVerifyResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.requestEmail(email, api)
            _emailVerifyResponse.postValue(Event(result))
        }
    }
    fun checkNickname(nickname: String, api: AuthApi) {
        _nicknameResponse.postValue(Event(Resource.Loading()))
        viewModelScope.launch {
            val result = authRepository.checkNickname(nickname, api)
            _nicknameResponse.postValue(Event(result))
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