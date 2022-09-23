package com.audifytech.task.audiotask.services

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewModelScope
import com.audifytech.task.audiotask.PlayerNotificationManager
import com.audifytech.task.audiotask.log
import com.audifytech.task.audiotask.model.MediaStoreAudioRepository
import com.audifytech.task.audiotask.model.data.Audio
import com.audifytech.task.audiotask.model.data.LoadableState
import com.audifytech.task.audiotask.view.PlayerControllerWidget
import com.audifytech.task.audiotask.view.WIDGET_ARTIST
import com.audifytech.task.audiotask.view.WIDGET_IS_PLAYING
import com.audifytech.task.audiotask.view.WIDGET_TITLE
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.Listener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class PlayerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val audioRepository = MediaStoreAudioRepository()
    private val notificationManager by lazy {
        PlayerNotificationManager(this)
    }
    private val componentName by lazy {
        ComponentName(applicationContext, PlayerControllerWidget::class.java)
    }
    private lateinit var player: ExoPlayer
    private var reloadJob: Job? = null
    private val binder = PlayerServiceBinder()
    private var audioPlaylist: List<Audio> = emptyList()
        set(value) {
            field = value
            _currentPlaylist.value = value
        }
    private val _currentPlaylist = MutableStateFlow(audioPlaylist)
    val currentPlaylist = _currentPlaylist.asStateFlow()

    private val playerListener = object : Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateExternalControllers(player.isPlaying)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateExternalControllers(isPlaying)
        }
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(applicationContext).build()
        reloadPlaylist()
        player.addListener(playerListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
        player.removeListener(playerListener)
        reloadJob?.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            log { "Received action request" }
            if (action != ACTIONS_STOP) {
                startForeground()
            }
            when (action) {
                ACTIONS_PLAY -> play()
                ACTIONS_PAUSE -> pause()
                ACTIONS_STOP -> stopPlayer()
                ACTIONS_PREVIOUS -> previous()
                ACTIONS_NEXT -> next()
            }
        }
        return START_STICKY
    }

    fun play(index: Int) {
        player.seekTo(index, 0)
        play()
    }

    fun play() {
        player.prepare()
        player.play()
        startForeground()
    }

    fun pause() {
        player.pause()
    }

    fun isPlaying() = player.isPlaying
    fun currentMediaItem() = player.currentMediaItem
    fun currentMediaDuration() = player.duration
    fun currentMediaIndex() = player.currentMediaItemIndex
    fun currentSeekPosition() = player.currentPosition

    fun previous() {
        player.seekToPreviousMediaItem()
    }

    fun next() {
        player.seekToNextMediaItem()
    }

    fun stopPlayer() {
        player.stop()
        notificationManager.cancelNotification()
        //stopSelf()
    }

    fun seek(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun reloadPlaylist() {
        reloadJob = serviceScope.launch {
            val audios = audioRepository.fetchAudios(applicationContext)
            this@PlayerService.audioPlaylist = audios
            val mediaItems = audios.mapNotNull { audio -> audio.uri?.let { MediaItem.fromUri(it) } }
            withContext(Dispatchers.Main) {
                player.setMediaItems(mediaItems)
            }
        }
    }

    fun addListener(listener: Listener) = player.addListener(listener)
    fun removeListener(listener: Listener) = player.removeListener(listener)

    private fun updateExternalControllers(isPlaying: Boolean) {
        val currentIdx = currentMediaIndex()
        val title = audioPlaylist[currentIdx].title
        val artists = audioPlaylist[currentIdx].artists.joinToString(separator = ", ")
        notificationManager.generateNotification(
            null,
            audioPlaylist[currentIdx].title,
            audioPlaylist[currentIdx].artists.joinToString(separator = ", "),
            isPlaying
        )
       sendBroadcast(Intent(applicationContext, PlayerControllerWidget::class.java).also { intent ->
           intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
           intent.putExtra(WIDGET_TITLE, title)
           intent.putExtra(WIDGET_ARTIST, artists)
           intent.putExtra(WIDGET_IS_PLAYING, isPlaying)
           val ids: IntArray = AppWidgetManager.getInstance(application).getAppWidgetIds(componentName)
           intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
       })
    }

    private fun startForeground() {
        notificationManager.createMediaNotification(
            null,
            player.currentMediaItem?.mediaMetadata?.title.toString(),
            player.currentMediaItem?.mediaMetadata?.artist.toString(),
            true
        )
    }

    override fun onBind(indent: Intent?): IBinder? = binder

    inner class PlayerServiceBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }
}