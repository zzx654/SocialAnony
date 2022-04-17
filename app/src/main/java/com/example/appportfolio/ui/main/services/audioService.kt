package com.example.appportfolio.ui.main.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.appportfolio.R
import com.example.appportfolio.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.appportfolio.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.appportfolio.other.Constants.NOTIFICATION_ID
import com.example.appportfolio.other.Constants.TOGGLE_PLAY
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class audioService:LifecycleService() {

    @Inject
    @Named("mediaNoti")
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    lateinit var curNotificationBuilder: NotificationCompat.Builder
    var isFirstRun=true
    var isPlaying=false
    //var mediaPlayer: MediaPlayer? = null
    var mediaposition:Int?=0
    lateinit var progressJob: Job
    lateinit var rviews:RemoteViews
    companion object{
        val mediaPlayer=MutableLiveData<MediaPlayer?>()
        val isplaying = MutableLiveData<Boolean>()
        val nicknameText = MutableLiveData<String>()
        val curpos = MutableLiveData<Event<Resource<Int>>>()
        val elapsedStr = MutableLiveData<String>()
        val mediamax = MutableLiveData<Int>()
    }
    fun initialvalues()
    {
        mediaPlayer.value?.let{
            it.stop()
            it.release()
            if (::progressJob.isInitialized) progressJob.cancel()
        }

        isplaying.value=false

        curpos.postValue(Event(Resource.Success(0)))
        val mediamax = MutableLiveData<Int>()
    }
    inner class mBinder: Binder(){
        fun getService():audioService{
            return this@audioService
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }
    var binder=mBinder()
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                TOGGLE_PLAY->
                {
                    toggle_play()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder=baseNotificationBuilder

        subscribeToObserver()
    }
    fun subscribeToObserver()
    {
        isplaying.observe(this){
            isPlaying=it
        }
    }
    fun toggle_play()
    {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val notibuilder=curNotificationBuilder.setContent(rviews)
        val notification=notibuilder.build()
        updateRemoteView(rviews,notification)
        notificationManager.notify(NOTIFICATION_ID,notification)
    }
    private fun updateRemoteView(remoteViews: RemoteViews,notification:Notification)
    {
        if(isPlaying)
        {
            mediaPlayer.value?.pause()
            mediaposition=mediaPlayer.value?.currentPosition
            isplaying.postValue(false)
            remoteViews.setImageViewResource(R.id.btn_play_pause,R.drawable.ic_play)
        }
        else{
            mediaPlayer.value?.seekTo(mediaposition!!)
            mediaPlayer.value?.start()
            startPlayback()
            isplaying.postValue(true)
            remoteViews.setImageViewResource(R.id.btn_play_pause,R.drawable.ic_pause)
        }
    }
    private fun createRemoteView(layoutId:Int):RemoteViews
    {
        val remoteView= RemoteViews(this.packageName,layoutId)
        val actionTogglePlay=Intent(TOGGLE_PLAY)
        val togglePlay=PendingIntent.getService(this,0,actionTogglePlay,0)
        remoteView.setOnClickPendingIntent(R.id.btn_play_pause,togglePlay)
        return remoteView
    }
    fun setMedia(path:String,nickname:String)
    {
        nicknameText.value=nickname+"님의 음성"
        if(isFirstRun!!) {
            isFirstRun = false
            rviews=createRemoteView(R.layout.notification_player)
            rviews.setTextViewText(R.id.txt_title, nicknameText.value!!)
            startForegroundService()
        }
        var audiosource=path
        mediaPlayer.value = MediaPlayer()
            .apply {
                reset()
                setDataSource(audiosource)
                prepare() // 재생 할 수 있는 상태 (큰 파일 또는 네트워크로 가져올 때는 prepareAsync() )
            }
        mediaPlayer.value?.setOnCompletionListener {

            isplaying.postValue(false)
            curpos.value=Event(Resource.Success(0))
            mediaposition=0
            stopPlayback()

            rviews.setImageViewResource(R.id.btn_play_pause,R.drawable.ic_play)
            rviews.setProgressBar(R.id.mediaProgress, mediaPlayer.value!!.duration,0,false)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            val notibuilder=curNotificationBuilder.setContent(rviews)
            val notification=notibuilder.build()
            notificationManager.notify(NOTIFICATION_ID,notification)
        }
        mediamax.value=mediaPlayer.value!!.duration
    }
    fun startPlayback() {
        if (::progressJob.isInitialized) progressJob.cancel()
        var a=0
        curpos.postValue(Event(Resource.Success(mediaposition!!)))
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (mediaPlayer.value!!.currentPosition < mediaPlayer.value!!.duration) {
                delay(20L)
                a=(a+20)%500
                curpos.postValue(Event(Resource.Success(mediaPlayer.value!!.currentPosition)))
                if(a==0)
                {
                    rviews.setProgressBar(R.id.mediaProgress, mediaPlayer.value!!.duration,mediaPlayer.value!!.currentPosition,false)
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                            as NotificationManager
                    val notibuilder=curNotificationBuilder.setContent(rviews)
                    val notification=notibuilder.build()
                    notificationManager.notify(NOTIFICATION_ID,notification)
                }
            }
        }
    }
    fun stopPlayback()
    {
        if (::progressJob.isInitialized) progressJob.cancel()
    }

    private fun startForegroundService() {
        val notibuilder=curNotificationBuilder.setContent(rviews)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID, notibuilder.build())
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
    override fun onDestroy() {
        initialvalues()

        super.onDestroy()
    }
}