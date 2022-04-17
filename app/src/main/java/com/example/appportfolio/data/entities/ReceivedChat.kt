package com.example.appportfolio.data.entities

data class ReceivedChat (
    val dateChanged:Int,
    val profileimage:String?,
    val nickname:String,
    val gender:String,
    val senderid:Int,
    val roomid:String,
    val date:String,
    val type:String,
    val content:String,

        )