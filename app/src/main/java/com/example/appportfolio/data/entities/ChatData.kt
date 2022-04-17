package com.example.appportfolio.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
        tableName="chatcontents"
)
data class ChatData (
        @PrimaryKey(autoGenerate=true)
        var id:Int?=null,
        val senderid:Int?,
        val roomid:String,
        val date:String,
        val type:String,
        val content:String,
        val isread:Int?
        ):Serializable