package com.example.appportfolio.repositories

import com.example.appportfolio.data.entities.ChatData
import com.example.appportfolio.db.ChatDatabase
import javax.inject.Inject

class ChatRepository @Inject constructor(private val db:ChatDatabase) {

    suspend fun insertChat(chatdata: ChatData) = db.getChatDao().insertChat(chatdata)

    fun getAllChats() = db.getChatDao().getAllChats()

    suspend fun readChats(roomid:String) = db.getChatDao().readChats(roomid)

    fun loadimages(roomid:String) = db.getChatDao().loadimages(roomid)

    fun loadchatContents(roomid: String) = db.getChatDao().loadchatContents(roomid)

    fun loadbeforechatContents(roomid:String,id:Int) = db.getChatDao().loadbeforechatContents(roomid,id)

    fun deleteroom(roomid:String) = db.getChatDao().deleteroom(roomid)

    fun deleteAllroom() = db.getChatDao().deleteAllroom()



}