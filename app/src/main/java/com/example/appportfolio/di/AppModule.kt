package com.example.appportfolio.di

import android.content.Context
import com.example.appportfolio.db.ChatDatabase
import com.example.appportfolio.repositories.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideApplicationContext(
        @ApplicationContext context: Context
    )=context

    @Singleton
    @Provides
    fun provideMainDispatcher()= Dispatchers.Main as CoroutineDispatcher

    @Singleton
    @Provides
    fun provideChatRepository(chatDatabase: ChatDatabase)= ChatRepository(chatDatabase)

    @Singleton
    @Provides
    fun provideChatDatabase(@ApplicationContext context: Context)=ChatDatabase(context)


}