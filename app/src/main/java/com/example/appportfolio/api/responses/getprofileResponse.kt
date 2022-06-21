package com.example.appportfolio.api.responses

data class getprofileResponse(
    val resultCode:Int,
    val platform:String?,
    val account:String?,
    val profileimage:String?,
    val nickname:String,
    val gender:String,
    val followercount:Int?,
    val followingcount:Int?,
    val postscount:Int?,
    val age:Int?
)