package com.audifytech.task.audiotask.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.audifytech.task.audiotask.log
import com.audifytech.task.audiotask.model.LikedAudioDataStore
import com.audifytech.task.audiotask.model.RoomLikedAudioDataStore
import com.audifytech.task.audiotask.model.data.Audio
import com.audifytech.task.audiotask.model.data.LoadableState
import com.audifytech.task.audiotask.model.db.AudioExtrasRoomDatabase
import com.audifytech.task.audiotask.services.PlayerService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Long.max


class PlaylistViewModel(application: Application): AndroidViewModel(application) {
    private val likedAudioDataStore: LikedAudioDataStore = RoomLikedAudioDataStore(AudioExtrasRoomDatabase.getInstance(application.applicationContext).likedAudioDao())
    private val serviceConnection = PlayerServiceConnection()
    private val _currentSeekPosition = MutableStateFlow(0L)
    private val _maxSeekPosition = MutableStateFlow(0L)
    private val _currentIndex = MutableStateFlow(-1)
    private val _playlist = MutableStateFlow<LoadableState<List<Audio>>>(LoadableState.Loading())
    private val _isPlaying = MutableStateFlow(false)
    val currentSeekPosition = _currentSeekPosition.asStateFlow()
    val maxSeekPosition = _maxSeekPosition.asStateFlow()
    val currentIndex = _currentIndex.asStateFlow()
    val playlist = _playlist.asStateFlow()
    val isPlaying = _isPlaying.asStateFlow()

    init {
        application.startService(Intent(application.applicationContext, PlayerService::class.java))
        application.bindService(Intent(application.applicationContext, PlayerService::class.java), serviceConnection, Context.BIND_IMPORTANT)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }

    fun isLiked(id: Long) = likedAudioDataStore.isLiked(id)
    fun setLiked(id: Long, state: Boolean) = likedAudioDataStore.setLiked(id, state)

    fun play(idx: Int) {

        viewModelScope.launch {
            serviceConnection.withService { service ->
                service.play(idx)
            }
        }
    }

    fun play() {
        viewModelScope.launch {
            serviceConnection.withService { service ->
                service.play()
            }
        }
    }

    fun pause() {
        viewModelScope.launch {
            serviceConnection.withService { service ->
                service.pause()
            }
        }
    }

    fun next() {
        viewModelScope.launch {
            serviceConnection.withService { service ->
                service.next()
            }
        }
    }

    fun previous() {
        viewModelScope.launch {
            serviceConnection.withService { service ->
                service.previous()
            }
        }
    }

    fun stop() {
        viewModelScope.launch {
            serviceConnection.withService { service ->
                service.stopPlayer()
            }
        }
    }

    fun seekSlider(position: Long) {
        viewModelScope.launch {
            serviceConnection.withService { service ->
                service.seek(position)
            }
        }
    }



    inner class PlayerServiceConnection: ServiceConnection {
        val connectionScope = CoroutineScope(Dispatchers.IO)
        private var timelineTicker: Job? = null
        private val playerListener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentIndex.value = service?.currentMediaIndex() ?: -1
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    val realDurationMillis: Long = service?.currentMediaDuration() ?: 0
                    _maxSeekPosition.value = max(0, realDurationMillis)
                }
            }

         /* override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (!timeline.isEmpty) {
                    val durationMs = max(0, timeline.getPeriod(0, Timeline.Period()).durationMs)
                    _maxSeekPosition.value = durationMs
                }
            }*/

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    timelineTicker = viewModelScope.launch {
                        while (true) {
                            withService {
                                service?.currentSeekPosition()?.let { pos ->
                                    _currentSeekPosition.value = pos
                                }
                            }
                            delay(1000)
                        }
                    }
                } else {
                    timelineTicker?.cancel()
                }
            }
        }
        private var service: PlayerService? = null
        private var bound = false
        private var completableDeferred = CompletableDeferred<Unit>(null)
        private var playlistCollectJob: Job? = null
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (binder !is PlayerService.PlayerServiceBinder) {
                return
            }
            service = binder.getService()
            bound = true
            completableDeferred.complete(Unit)
            service!!.addListener(playerListener)
            playlistCollectJob = connectionScope.launch {
                service?.currentPlaylist?.collectLatest {
                    this@PlaylistViewModel._playlist.value = LoadableState.Loaded(it)
                    withContext(Dispatchers.Main) {
                        this@PlaylistViewModel._currentIndex.value = service?.currentMediaIndex() ?: -1
                    }
                }
            }
            log { "Bound PlayerService to ${javaClass.simpleName}" }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service!!.removeListener(playerListener)
            service = null
            bound = false
            val oldCompletableDeferred = completableDeferred
            completableDeferred = CompletableDeferred(null)
            completableDeferred.invokeOnCompletion { oldCompletableDeferred.complete(Unit) }
            playlistCollectJob?.cancel()
            log { "Unbound PlayerService from ${javaClass.simpleName}" }
        }

        suspend fun withService(consumer: suspend (PlayerService) -> Unit) {
            completableDeferred.await()
            consumer(service!!)
        }
    }
}