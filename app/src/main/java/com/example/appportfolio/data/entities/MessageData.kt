package com.example.appportfolio.data.entities

data class MessageData(
    val num:Int?,
    val senderid:Int?,
    val date:String,
    val type:String,
    val content:String,
    var isread:Int,
    var nickname:String?,
    var gender:String?,
    var profileimage:String?,
    var dateChanged:Boolean
    
)