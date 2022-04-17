package com.example.appportfolio.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationLatLngEntity (
    val lat:Float,
    val lon:Float
        ):Parcelable