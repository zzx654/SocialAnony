package com.example.appportfolio.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatImage(
    val date:String,
    val imageUrl:String,
    var isChecked:Boolean
): Parcelable