package com.example.appportfolio.repositories

import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.other.Resource
import com.example.appportfolio.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository{
    suspend fun changepassword(
        curpw: String,
        newpw:String,
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.changepassword(curpw,newpw))
        }
    }
    suspend fun findpassword(
        email: String,
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.findpassword(email))
        }
    }
    suspend fun withdrawal(
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.withdrawal())
        }
    }
    suspend fun getUserid(
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getUserid())
        }
    }
    suspend fun getmyprofile(
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getmyprofile())
        }
    }
    suspend fun togglechat(
        toggle:Int,
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.togglechat(toggle))
        }
    }
    suspend fun getChatonoff(
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getChatonoff())
        }
    }
    suspend fun checkfcmtoken(
        fcmtoken: String,
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.checkfcmtoken( fcmtoken))
        }
    }

    suspend fun requestVerify(
        phone:String,
        api:AuthApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.requestVerify(phone))
        }
    }

    suspend fun autologin(
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.autologin())
        }
    }
    suspend fun checkProfile(
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.checkProfile())
        }
    }

    suspend fun login(
        email:String,
        password:String,
        fcmToken:String,
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.login(email,password,fcmToken))
        }
    }
    suspend fun register(
        email:String,
        password:String,
        code:String,
        phone:String,
        api: AuthApi
    )= withContext(Dispatchers.IO) {
        safeCall{
            Resource.Success(api.register(email,password,code,phone))
        }
    }
    suspend fun AuthComplete(
        profileimage:String?,
        nickname:String,
        gender:String,
        age:String,
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.authcomplete(profileimage,nickname,gender,age).message)
        }
    }
    suspend fun signWithSocial(
        platform: String,
        account: String,
        fcmtoken: String,
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.signWithSocial(platform,account,fcmtoken))
        }
    }

    suspend fun logout(
        api: AuthApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.logout())
        }
    }

}