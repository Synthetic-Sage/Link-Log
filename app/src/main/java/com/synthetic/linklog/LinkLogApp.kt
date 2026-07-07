package com.synthetic.linklog

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.synthetic.linklog.util.YoutubeDlManager

@HiltAndroidApp
class LinkLogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            YoutubeDlManager.init(this@LinkLogApp)
        }
    }
}
