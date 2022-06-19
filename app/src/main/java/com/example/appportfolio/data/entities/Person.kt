package com.example.appportfolio.data.entities

data class Person (
    val userid:Int?,
    val nickname:String,
    val gender:String,
    val profileimage:String,
    var following:Int,
    val followingcount:Int?
        )