package com.example.appportfolio.data.entities
data class SendData(
    val roomid:String,
    val senderid:Int,
    val receiverid:Int,
    val content:String,
    val type:String,
    val sendTime:String,
    val dateChanged:Boolean,
    val changedDate:String
)