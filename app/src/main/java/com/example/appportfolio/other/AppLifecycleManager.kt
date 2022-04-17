package com.example.appportfolio.other

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

object AppLifecycleManager:LifecycleObserver {
    var isForeground=false
    var isDestroyed=true
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() { isForeground = false }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() { isForeground = true
        isDestroyed=false
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onAppCreated() {  }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onAppResumed() {}

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroyed() {
        isDestroyed=true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onAppPaused() {  }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onAppAny() {  }
}