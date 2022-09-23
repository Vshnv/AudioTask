package com.audifytech.task.audiotask.view

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import com.audifytech.task.audiotask.R
import com.audifytech.task.audiotask.services.*

const val WIDGET_IS_PLAYING = "widget.extras.is_playing"
const val WIDGET_TITLE = "widget.extras.title"
const val WIDGET_ARTIST = "widget.extras.artist"
const val WIDGET_THUMBNAIL = "widget.extras.thumbnail"
const val WIDGET_CONTROLS_REQUEST_CODE = 4563

class PlayerControllerWidget : AppWidgetProvider() {
    var isPlaying: Boolean = false
    var title = "..."
    var artist = "..."
    var thumbnail: ByteArray? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val extras = intent.extras
                isPlaying = extras?.getBoolean(WIDGET_IS_PLAYING) ?: false
                title = extras?.getString(WIDGET_TITLE) ?: "Unknown"
                artist = extras?.getString(WIDGET_ARTIST) ?: "<unknown>"
                thumbnail = extras?.getByteArray(WIDGET_THUMBNAIL)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val playIntent = context.pendingIntent(ACTIONS_PLAY)
        val pauseIntent = context.pendingIntent(ACTIONS_PAUSE)
        val nextIntent = context.pendingIntent(ACTIONS_NEXT)
        val prevIntent = context.pendingIntent(ACTIONS_PREVIOUS)
        val stopIntent = context.pendingIntent(ACTIONS_STOP)
        val remoteViews = RemoteViews(context.packageName, R.layout.player_controller_widget)
        remoteViews.setTextViewText(R.id.audio_title, title)
        remoteViews.setTextViewText(R.id.audio_artist, artist)
        if (isPlaying) {
            remoteViews.setOnClickPendingIntent(R.id.widget_btn_play, pauseIntent)
            remoteViews.setImageViewResource(R.id.widget_btn_play, R.drawable.icons_pause)
        } else {
            remoteViews.setOnClickPendingIntent(R.id.widget_btn_play, playIntent)
            remoteViews.setImageViewResource(R.id.widget_btn_play, R.drawable.icons_play)
        }
        remoteViews.setOnClickPendingIntent(R.id.widget_btn_next, nextIntent)
        remoteViews.setOnClickPendingIntent(R.id.widget_btn_prev, prevIntent)
        remoteViews.setOnClickPendingIntent(R.id.widget_btn_stop,stopIntent)
        thumbnail?.let { thumbnailBytes ->
            val bitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size)
            remoteViews.setImageViewBitmap(R.id.audio_thumbnail, bitmap)
        }
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    private fun Context.pendingIntent(action: String) = PendingIntent.getBroadcast(
        this,
        WIDGET_CONTROLS_REQUEST_CODE,
        Intent(action).setPackage(this.packageName),
        if (action == ACTIONS_STOP) PendingIntent.FLAG_CANCEL_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT
    )
}