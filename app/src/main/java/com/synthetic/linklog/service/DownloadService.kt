package com.synthetic.linklog.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.synthetic.linklog.data.local.entity.DownloadStatus
import com.synthetic.linklog.data.repository.DownloadRepository
import com.synthetic.linklog.data.repository.SettingsRepository
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File
import androidx.documentfile.provider.DocumentFile
import android.net.Uri

@AndroidEntryPoint
class DownloadService : Service() {

    @Inject
    lateinit var downloadRepository: DownloadRepository
    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var activeDownloads = 0

    companion object {
        const val ACTION_START_DOWNLOAD = "ACTION_START_DOWNLOAD"
        const val EXTRA_LINK_ID = "EXTRA_LINK_ID"
        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_TITLE = "EXTRA_TITLE"

        private const val CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "DownloadService"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_DOWNLOAD) {
            val linkId = intent.getLongExtra(EXTRA_LINK_ID, -1)
            val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
            val title = intent.getStringExtra(EXTRA_TITLE) ?: "Video"

            if (linkId != -1L) {
                startForeground(NOTIFICATION_ID, createNotification("Starting download...", 0))
                activeDownloads++
                startDownload(linkId, url, title)
            }
        }
        return START_NOT_STICKY
    }

    private fun startDownload(linkId: Long, url: String, title: String) {
        serviceScope.launch {
            try {
                // Mark as downloading in DB
                downloadRepository.updateProgress(linkId, 0, DownloadStatus.DOWNLOADING)

                // Fetch Settings
                val saveUriString = settingsRepository.saveLocationUri.first()
                val videoQuality = settingsRepository.videoQuality.first()
                val audioQuality = settingsRepository.audioQuality.first()
                val format = settingsRepository.downloadFormat.first()

                val defaultDir = File(getExternalFilesDir(null), "Downloads")
                if (!defaultDir.exists()) defaultDir.mkdirs()
                
                var outDir = defaultDir.absolutePath
                if (saveUriString != null) {
                    try {
                        val treeUri = Uri.parse(saveUriString)
                        val docFile = DocumentFile.fromTreeUri(this@DownloadService, treeUri)
                        if (docFile != null && docFile.canWrite()) {
                            // Note: YoutubeDL requires a real file path or a specialized storage setup
                            // For simplicity, we download to cache/files dir and move to SAF, or use the file descriptor.
                            // youtubedl-android has limited support for SAF directly.
                            // We will download to local app files dir and then move to the user selected location.
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse save location URI", e)
                    }
                }

                // Temporary download path
                val tempDir = File(cacheDir, "yt_dlp_temp").apply { mkdirs() }
                
                val request = YoutubeDLRequest(url)
                
                // Format settings
                if (format == "mp3") {
                    request.addOption("--extract-audio")
                    request.addOption("--audio-format", "mp3")
                    if (audioQuality == "best") request.addOption("--audio-quality", "0")
                    else request.addOption("--audio-quality", "5")
                } else {
                    if (videoQuality == "1080p") request.addOption("-f", "bestvideo[height<=1080]+bestaudio/best[height<=1080]")
                    else if (videoQuality == "720p") request.addOption("-f", "bestvideo[height<=720]+bestaudio/best[height<=720]")
                    else request.addOption("-f", "best") // best
                    request.addOption("--merge-output-format", format)
                }

                request.addOption("--embed-metadata")
                request.addOption("-o", "${tempDir.absolutePath}/%(title)s.%(ext)s")

                YoutubeDL.getInstance().execute(request, TAG) { progress, etaInSeconds, line ->
                    updateNotificationProgress(title, progress.toInt())
                    serviceScope.launch {
                        downloadRepository.updateProgress(linkId, progress.toInt(), DownloadStatus.DOWNLOADING)
                    }
                }

                // Download complete - find the file
                val downloadedFile = tempDir.listFiles()?.firstOrNull()
                
                if (downloadedFile != null && downloadedFile.exists()) {
                    // Move to final destination
                    val destFile = File(outDir, downloadedFile.name)
                    downloadedFile.copyTo(destFile, overwrite = true)
                    downloadedFile.delete()

                    // Update DB with final local URI
                    val video = downloadRepository.getDownload(linkId)
                    if (video != null) {
                        downloadRepository.insertOrUpdate(video.copy(
                            localUri = destFile.absolutePath,
                            status = DownloadStatus.COMPLETED,
                            downloadProgress = 100,
                            fileSize = destFile.length()
                        ))
                    }
                    updateNotificationProgress("$title - Completed", 100)
                } else {
                    throw Exception("Downloaded file not found in temp dir")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                downloadRepository.updateProgress(linkId, 0, DownloadStatus.FAILED)
                updateNotificationProgress("$title - Failed", 0)
            } finally {
                activeDownloads--
                if (activeDownloads <= 0) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Link Log Download")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .build()
    }

    private fun updateNotificationProgress(title: String, progress: Int) {
        val notification = createNotification(title, progress)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
