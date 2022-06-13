package com.example.appportfolio.di

import android.content.Context
import com.example.appportfolio.adapters.*
import com.example.appportfolio.repositories.AuthRepository

import com.example.appportfolio.auth.SignManager
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.repositories.LocRepository
import com.example.appportfolio.repositories.MainRepository
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.dialog.DownloadProgressDialog
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.dialog.UploadProgressDialog

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object MainModule {

    @Provides
    @ActivityScoped
    fun provideUploadDialogProgress(@ActivityContext context:Context): UploadProgressDialog
    {
        return UploadProgressDialog(context)
    }
    @Provides
    @ActivityScoped
    fun provideLoadingDialogProgress(@ActivityContext context:Context): LoadingDialog
    {
        return LoadingDialog(context)
    }
    @Provides
    @ActivityScoped
    fun provideDownloadDialogProgress(@ActivityContext context:Context): DownloadProgressDialog
    {
        return DownloadProgressDialog(context)
    }
    @Provides
    @ActivityScoped
    fun provideChatReqeustsAdapter()=ChatRequestsAdapter()
    @Provides
    @ActivityScoped
    fun provideChatRoomAdapter()=ChatRoomAdapter()
    @Provides
    @ActivityScoped
    fun provideBlockAdapter()= BlockAdapter()
    @Provides
    @ActivityScoped
    fun provideNotiAdapter()= NotiAdapter()
    @Provides
    @ActivityScoped
    fun provideImagesAdapter()= ImagesAdapter()

    //@Provides
    //@ActivityScoped
    //fun provideCommentAdapter()= CommentAdapter()
    @Provides
    @ActivityScoped
    fun providePostImageAdapter()= PostImageAdapter()

    @Provides
    @ActivityScoped
    fun provideAuthRepository()= AuthRepository()

    @Provides
    @ActivityScoped
    fun provideMainRepository()= MainRepository()
    @Provides
    @ActivityScoped
    fun provideLocRepository()= LocRepository()
    @Provides
    @ActivityScoped
    fun providePostAdapter()= PostAdapter()

    @Provides
    @ActivityScoped
    fun provideChatAdapter()= ChatAdapter()


    @Provides
    @ActivityScoped
    fun provideSignManager(@ActivityContext context:Context):SignManager
    {
        return SignManager(context)
    }

    @Provides
    @ActivityScoped
    fun provideGpsTracker(@ActivityContext context:Context):GpsTracker
    {
        return GpsTracker(context)
    }

    @Provides
    @ActivityScoped
    fun provideUserPreference(@ActivityContext context:Context):UserPreferences
    {
        return UserPreferences(context)
    }
}