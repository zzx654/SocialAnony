package com.example.appportfolio.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Voteoptions (
    val options:List<Voteoption>?
        ):Parcelable