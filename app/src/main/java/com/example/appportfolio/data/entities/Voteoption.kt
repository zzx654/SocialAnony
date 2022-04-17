package com.example.appportfolio.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Voteoption (
    var option:String,
    val position:String
        ):Parcelable