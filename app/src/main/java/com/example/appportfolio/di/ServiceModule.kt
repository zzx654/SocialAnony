package com.example.appportfolio.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.appportfolio.R
import com.example.appportfolio.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.appportfolio.repositories.UploadRepository
import com.example.appportfolio.ui.main.activity.MainActivity

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Named

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {


    @ServiceScoped
    @Provides
    fun providePostActivityPendingIntent(
        @ApplicationContext app:Context
    )=PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    @ServiceScoped
    @Provides
    @Named("recordNoti")
    fun provideRecordNoticicationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app,NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_record)
        .setContentTitle("녹음중")
        .setContentText("0:00")
        .setContentIntent(pendingIntent)

    @ServiceScoped
    @Provides
    @Named("mediaNoti")
    fun provideMediaNoticicationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app,NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_play)
        .setContentTitle("녹음중")
        .setContentIntent(pendingIntent)

    @Provides
    @ServiceScoped
    fun provideUploadRepository()= UploadRepository()


}