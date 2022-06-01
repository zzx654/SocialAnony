package com.example.appportfolio.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    val postnum:Int,
    val postid:String?,
    val userid:Int,
    val account:String?,
    val platform:String?,
    val nickname:String,
    val anonymous:String?,
    val profileimage:String?,
    val gender:String?,
    val text:String,
    val tags:String?,
    val date:String,
    val image:String,
    val audio:String,
    var commentcount:Int,
    var likecount:Int,
    var bookmarked:Int?,
    var isLiked:Int?,
    var distance:Double?,
    val vote:String,
    val votecount:Int
):Parcelable