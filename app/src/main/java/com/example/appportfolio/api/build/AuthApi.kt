package com.example.appportfolio.api.build


import com.example.appportfolio.api.responses.*
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {

    @POST("/getUserid")
    suspend fun getUserid(
    ):getuserResponse

    @POST("/getmyprofile")
    suspend fun getmyprofile(

    ):getprofileResponse
    @FormUrlEncoded
    @POST("/togglechat")
    suspend fun togglechat(
        @Field("toggle")int:Int
    ):intResponse

    @FormUrlEncoded
    @POST("/findpassword")
    suspend fun findpassword(
        @Field("email")email:String
    ):intResponse
    @FormUrlEncoded
    @POST("/changepassword")
    suspend fun changepassword(
        @Field("curpw")curpw:String,
        @Field("newpw")newpw:String

    ):intResponse
    @POST("/getChatonoff")
    suspend fun getChatonoff(
    ):intResponse
    @FormUrlEncoded
    @POST("/checkNickname")
    suspend fun checkNickname(
        @Field("nickname")nickname : String
    ):NicknameResponse

    @FormUrlEncoded
    @POST("/requestEmail")
    suspend fun requestEmail(
        @Field("email")email : String
    ): VerifyResponse

    @FormUrlEncoded
    @POST("/authsms")
    suspend fun requestVerify(
        @Field("phone")phone : String
    ): VerifyResponse

    @FormUrlEncoded
    @POST("/verifycode")
    suspend fun verifycode(
        @Field("phone")phone : String,
        @Field("code")code : String
    ): VerifyResponse



    @FormUrlEncoded
    @POST("/register")
    suspend fun register(
        @Field("email")email : String,
        @Field("password")password :String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("/authcomplete")
    suspend fun authcomplete(
        @Field("nickname")nickname : String,
        @Field("gender")gender :String,
        @Field("age")age:String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("/api/login")
    suspend fun login(
        @Field("email")email : String,
        @Field("password")password :String,
        @Field("fcmToken")fcmToken:String
    ): LoginResponse

    @POST("/api/autologin")
    suspend fun autologin(): checkProfileResponse

    @FormUrlEncoded
    @POST("/SocialSign")
    suspend fun signWithSocial(
        @Field("platform")platform: String,
        @Field("account")account: String,
        @Field("fcmtoken")fcmtoken:String
    ): LoginResponse

    @POST("/checkProfile")
    suspend fun checkProfile(
    ): checkProfileResponse

    @FormUrlEncoded
    @POST("/checkfcmtoken")
    suspend fun checkfcmtoken(
        @Field("fcmtoken")fcmtoken:String
    ): intResponse

    @POST("/api/logout")
    suspend fun logout(
    ): LogoutResponse

    @POST("/withdrawal")
    suspend fun withdrawal(
    ): intResponse

    @POST("/getAccount")
    suspend fun getcurAccountInfo(): accountResponse

}