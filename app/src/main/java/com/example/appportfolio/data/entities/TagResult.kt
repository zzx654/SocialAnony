package com.example.appportfolio.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


data class TagResult(
    val tagname:String,
    var count:Int?,
    var isLiked:Int?
)