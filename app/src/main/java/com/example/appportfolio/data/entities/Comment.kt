package com.example.appportfolio.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment (
    var topfixed:Boolean=false,
    val commentid:Int?,
    val postid:String,
    val userid:Int,
    val text:String,
    val ref:Int,
    val time:String,
    val depth:Int,
    val platform:String,
    val account:String,
    val anonymous:String?,
    val profileimage:String?,
    val nickname:String,
    val age:Int,
    val gender:String,
    var replycount:Int?,
    var likecount:Int,
    var commentliked:Int
        ): Parcelable