package com.example.appportfolio.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appportfolio.data.entities.ChatData


@Dao
interface ChatDao {

    @Insert(onConflict= OnConflictStrategy.REPLACE)
    fun insertChat(chatdata: ChatData):Long

    @Query("SELECT * FROM chatcontents WHERE roomid=:roomid AND senderid!=:myid ORDER BY senderid DESC LIMIT 1")
    fun getOpponentChat(roomid: String,myid:Int):LiveData<List<ChatData>>
    @Query("SELECT *FROM chatcontents WHERE roomid=:roomid AND id>:lastchatid ORDER BY date desc")
    fun getLastChats(roomid: String,lastchatid:Int):LiveData<List<ChatData>>
    @Query("SELECT *FROM chatcontents WHERE roomid=:roomid ORDER BY date DESC LIMIT 1")
    fun getAddedChat(roomid: String):LiveData<ChatData>
    @Query("SELECT *FROM chatcontents WHERE id in (SELECT MAX(id) FROM chatcontents GROUP BY roomid) ORDER BY id desc")
    fun getAllChats():LiveData<List<ChatData>>

    @Query("UPDATE chatcontents SET isread=1 WHERE roomid=:roomid")
    fun readChats(roomid:String)

    @Query("DELETE FROM chatcontents WHERE roomid=:roomid")
    fun deleteroom(roomid: String)

    @Query("DELETE FROM chatcontents")
    fun deleteAllroom()

    @Query("SELECT *FROM chatcontents WHERE roomid=:roomid AND type=:image ORDER BY date DESC")
    fun loadimages(roomid:String,image:String="IMAGE"):LiveData<List<ChatData>>

    @Query("SELECT *FROM chatcontents WHERE roomid=:roomid AND type!=:start ORDER BY date DESC LIMIT 20")
    fun loadchatContents(roomid:String,start:String="start"):LiveData<List<ChatData>>

    @Query("SELECT *FROM chatcontents WHERE roomid=:roomid AND type!=:start AND id<:id ORDER BY id DESC LIMIT 20")
    fun loadbeforechatContents(roomid:String,id:Int,start:String="start"):LiveData<List<ChatData>>

}