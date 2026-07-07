package com.synthetic.linklog.util

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.yausername.ffmpeg.FFmpeg

object YoutubeDlManager {
    
    private const val TAG = "YoutubeDlManager"
    private var isInitialized = false

    suspend fun init(context: Context) {
        if (isInitialized) return
        withContext(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().init(context)
                FFmpeg.getInstance().init(context)
                isInitialized = true
                Log.d(TAG, "YoutubeDL and FFmpeg initialized successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize YoutubeDL or FFmpeg", e)
            }
        }
    }

    suspend fun updateYoutubeDl(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!isInitialized) {
                    init(context)
                }
                val status = YoutubeDL.getInstance().updateYoutubeDL(context)
                Log.d(TAG, "YoutubeDL update status: $status")
                true
            } catch (e: YoutubeDLException) {
                Log.e(TAG, "Failed to update YoutubeDL", e)
                false
            }
        }
    }
}
