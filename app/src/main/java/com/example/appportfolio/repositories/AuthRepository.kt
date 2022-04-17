package com.example.appportfolio.repositories

import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.other.Resource
import com.example.appportfolio.safeCall
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ActivityScoped
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
    suspend fun verifycode(
        phone: String,
        code:String,
        api:AuthApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.verifycode(phone,code))
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
    suspend fun requestEmail(
        email:String,
        api:AuthApi
    )= withContext(Dispatchers.IO){

        safeCall {
            Resource.Success(api.requestEmail(email))
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
        api: AuthApi
    )= withContext(Dispatchers.IO) {
        safeCall{
            Resource.Success(api.register(email,password).message)
        }
    }
    suspend fun AuthComplete(

        nickname:String,
        gender:String,
        age:String,
        api: AuthApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.authcomplete(nickname,gender,age).message)
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
    suspend fun getcurAccountInfo(
        api: AuthApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getcurAccountInfo())
        }
    }
    suspend fun logout(
        api: AuthApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.logout())
        }
    }

    suspend fun checkNickname(
        nickname: String,
        api:AuthApi)= withContext(Dispatchers.IO){
            safeCall{
                Resource.Success(api.checkNickname(nickname))
            }
    }

}