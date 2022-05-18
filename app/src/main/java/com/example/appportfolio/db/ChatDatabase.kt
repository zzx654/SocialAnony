package com.example.appportfolio.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appportfolio.data.entities.ChatData

@Database(
    entities=[ChatData::class],
    version=2
    ,exportSchema = false
)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun getChatDao():ChatDao

    companion object{
        @Volatile
    private var instance:ChatDatabase?=null
    private val LOCK=Any()

    operator fun invoke(context: Context)=instance?: synchronized(LOCK){//이름없이 호출되는 연산자함수
        //synchronized 블록안에는 동시에 다른스레드 접근 불가,instance가 null일때 실행
        instance?:createDatabase(context).also{ instance=it}
    }

    private fun createDatabase(context: Context)=
        Room.databaseBuilder(
            context.applicationContext,
            ChatDatabase::class.java,
            "chat_db.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    }
}