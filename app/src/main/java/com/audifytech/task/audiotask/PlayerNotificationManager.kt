package com.audifytech.task.audiotask

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.STOP_FOREGROUND_REMOVE
import com.audifytech.task.audiotask.services.*
import java.io.File


private const val NOTIFICATION_CHANNEL_ID = "AudioTaskPlayerService"
private const val NOTIFICATION_ID = 14235
private const val NOTIFICATION_REQUEST_CODE = 100

class PlayerNotificationManager(private val mService: PlayerService) {
    private var mStarted = false
    private val mNotificationManager: NotificationManager =
        mService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mNotificationBuilder: NotificationCompat.Builder? = null
    private val mPlayIntent: PendingIntent = PendingIntent.getBroadcast(
        mService, NOTIFICATION_REQUEST_CODE,
        Intent(ACTIONS_PLAY).setPackage(mService.packageName), PendingIntent.FLAG_UPDATE_CURRENT
    )
    private val mPauseIntent: PendingIntent = PendingIntent.getBroadcast(
        mService, NOTIFICATION_REQUEST_CODE,
        Intent(ACTIONS_PAUSE).setPackage(mService.packageName), PendingIntent.FLAG_UPDATE_CURRENT
    )
    private val mPreviousIntent: PendingIntent = PendingIntent.getBroadcast(
        mService, NOTIFICATION_REQUEST_CODE,
        Intent(ACTIONS_PREVIOUS).setPackage(mService.packageName), PendingIntent.FLAG_UPDATE_CURRENT
    )
    private val mNextIntent: PendingIntent = PendingIntent.getBroadcast(
        mService, NOTIFICATION_REQUEST_CODE,
        Intent(ACTIONS_NEXT).setPackage(mService.packageName), PendingIntent.FLAG_UPDATE_CURRENT
    )
    private val mStopIntent: PendingIntent = PendingIntent.getBroadcast(
        mService, NOTIFICATION_REQUEST_CODE,
        Intent(ACTIONS_STOP).setPackage(mService.packageName), PendingIntent.FLAG_CANCEL_CURRENT
    )

    fun createMediaNotification(thumbnail: Bitmap?, title: String, artist: String, isPlaying: Boolean) {
        if (!mStarted) {
            mStarted = true
            mService.startForeground(NOTIFICATION_ID, generateNotification(thumbnail, title, artist, isPlaying))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            mService.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "${mService.getString(R.string.app_name)} Music Controls"
        mNotificationManager.createNotificationChannel(channel)
    }

    fun cancelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mService.stopForeground(Service.STOP_FOREGROUND_REMOVE)
            mNotificationManager.cancel(NOTIFICATION_ID)
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID)
        }
        mStarted = false
    }

    fun generateNotification(thumbnail: Bitmap?, title: String, artist: String, isPlaying: Boolean): Notification? {
        if (mNotificationBuilder == null) {
            mNotificationBuilder = NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.icons_music)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        mService.resources,
                        R.drawable.icons_music
                    )
                )
                .setContentTitle(mService.getString(R.string.app_name))
                .setContentText(mService.getString(R.string.app_name))
                .setDeleteIntent(mStopIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSilent(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            }
        }
        val contentView = RemoteViews(mService.packageName, R.layout.player_notification_view)
        val contentViewExpanded =
            RemoteViews(mService.packageName, R.layout.player_notification_view_expanded)
        contentViewExpanded.setTextViewText(R.id.audio_title, title)
        contentViewExpanded.setTextViewText(R.id.audio_artist, artist)
        if (isPlaying) {
            contentViewExpanded.setOnClickPendingIntent(R.id.btn_play_expanded, mPauseIntent)
            contentView.setImageViewResource(R.id.btn_play, R.drawable.icons_pause)
            contentViewExpanded.setImageViewResource(R.id.btn_play_expanded, R.drawable.icons_pause)
        } else {
            contentViewExpanded.setOnClickPendingIntent(R.id.btn_play_expanded, mPlayIntent)
            contentView.setImageViewResource(R.id.btn_play, R.drawable.icons_play)
            contentViewExpanded.setImageViewResource(R.id.btn_play_expanded, R.drawable.icons_play)
        }
        contentViewExpanded.setOnClickPendingIntent(R.id.btn_next_expanded, mNextIntent)
        contentViewExpanded.setOnClickPendingIntent(R.id.btn_prev_expanded, mPreviousIntent)
        contentViewExpanded.setOnClickPendingIntent(R.id.btn_stop_expanded, mStopIntent)
        mNotificationBuilder!!
            .setCustomContentView(contentView)
            .setCustomBigContentView(contentViewExpanded)
            .setOngoing(true)
        thumbnail?.let {
            contentView.setImageViewBitmap(R.id.audio_thumbnail, it)
            contentViewExpanded.setImageViewBitmap(R.id.audio_thumbnail, it)
        }
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder?.build())
        return mNotificationBuilder?.build()
    }
}