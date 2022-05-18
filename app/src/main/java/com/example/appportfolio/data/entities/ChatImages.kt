package com.example.appportfolio.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatImages (
    val chatimages:List<ChatImage>?
    ): Parcelable