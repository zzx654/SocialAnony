package com.example.appportfolio.ui.main.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.isDestroyed
import com.example.appportfolio.SocialApplication.Companion.isForeground
import com.example.appportfolio.data.entities.ChatData
import com.example.appportfolio.repositories.ChatRepository
import com.example.appportfolio.ui.auth.activity.InitActivity
import com.example.appportfolio.ui.main.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var chatRepository: ChatRepository

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        Log.i("FirebaseMessaging", "New Token : $p0")
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit()
        val mainactivityintent= Intent(applicationContext, MainActivity::class.java)
        val initactivityintent= Intent(applicationContext, InitActivity::class.java)
        Log.e("FirebaseMessageing", "message : ${remoteMessage.data}")
        //if(remoteMessage.data.get("notitype").equals("chat")&&AppLifecycleManager.isDestroyed)
        if(remoteMessage.data["notitype"].equals("chat"))
        {
            val senderid= remoteMessage.data["senderid"]
            val roomid= remoteMessage.data["roomid"]
            val date= remoteMessage.data["date"]
            val type= remoteMessage.data["type"]
            val content= remoteMessage.data["content"]
            //val dateChanged=remoteMessage.data.get("dateChanged")
            val chatdata= ChatData(null,senderid!!.toInt(),roomid!!,date!!,type!!,content!!,0)
                CoroutineScope(Dispatchers.IO).launch {
                    chatRepository.insertChat(chatdata)
                }
        }
        if(isForeground||!sharedPreferences.getBoolean("pushonoff",true))
            return
        else if(remoteMessage.data["type"] !="EXIT")
        {
            val pm =
                getSystemService(Context.POWER_SERVICE) as PowerManager
            @SuppressLint("InvalidWakeLockTag") val wakeLock =
                pm.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK
                            or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG"
                )
            wakeLock.acquire(3000)
            wakeLock.release()
            val intent:Intent = if(isDestroyed)
                initactivityintent
            else
                mainactivityintent
            val title = remoteMessage.data["title"]
            val type = remoteMessage.data["notitype"]
            val message = remoteMessage.data["message"]
            //type가 채팅이라면 room db에
            val pendingIntent= PendingIntent.getActivity(
                applicationContext,
                UUID.randomUUID().hashCode(),
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )


            val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.apply{
                    enableLights(true)
                    lightColor= Color.RED
                    enableVibration(true)
                    description="notification"
                    notificationManager.createNotificationChannel(channel)
                }
            }
            val notification=getNotificationBuilder(title!!,message!!,type!!,pendingIntent).build()
            notificationManager.notify(1001,notification)
        }
    }
    private fun getNotificationBuilder(title:String,content:String,type:String,pendingIntent:PendingIntent)

            : NotificationCompat.Builder{
        val type=if((type == "comment") || (type == "chat")) R.drawable.ic_stat_sms else R.drawable.ic_stat_favorite
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setSmallIcon(type)
            .setColor(0xff000000.toInt())
            .setShowWhen(true)
    }
    companion object {
        private const val CHANNEL_NAME = "Emoji Party"
        private const val CHANNEL_ID = "Channel Id"
    }

}