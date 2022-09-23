package com.audifytech.task.audiotask.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audifytech.task.audiotask.R
import com.audifytech.task.audiotask.model.data.Audio
import com.audifytech.task.audiotask.model.data.LoadableState
import com.audifytech.task.audiotask.ui.theme.AudioTaskTheme
import com.audifytech.task.audiotask.viewmodel.PlaylistViewModel

class PlaylistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playlistViewModel by viewModels<PlaylistViewModel>()
        setContent {
            AudioTaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    HomeScreen(playlistViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun HomeScreen(viewModel: PlaylistViewModel) {
    val currentMediaIndex = viewModel.currentIndex.collectAsStateWithLifecycle()
    val playlist = viewModel.playlist.collectAsStateWithLifecycle()
    val playing = viewModel.isPlaying.collectAsStateWithLifecycle()
    val maxTimeline = viewModel.maxSeekPosition.collectAsStateWithLifecycle()
    val curTimeline = viewModel.currentSeekPosition.collectAsStateWithLifecycle()
    PlaylistController(
        currentMediaIndex = currentMediaIndex,
        playlist = playlist,
        playing = playing,
        maxTimeline = maxTimeline,
        curTimeline = curTimeline,
        play = {
            if (it != null) {
                viewModel.play(it)
            } else {
                viewModel.play()
            }
        },
        pause = viewModel::pause,
        previous = viewModel::previous,
        next = viewModel::next,
        stop = viewModel::stop,
        seek = viewModel::seekSlider,
        isLikedRequester = {
            viewModel.isLiked(it)
        },
        like = viewModel::setLiked
    )
}

@Composable
fun PlaylistController(
    currentMediaIndex: State<Int>,
    playlist: State<LoadableState<List<Audio>>>,
    playing: State<Boolean>,
    maxTimeline: State<Long>,
    curTimeline: State<Long>,
    play: (Int?) -> Unit,
    pause: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    stop: () -> Unit,
    seek: (Long) -> Unit,
    isLikedRequester: (Long) -> LiveData<Boolean>,
    like: (Long, Boolean) -> Unit
) {
    val plValue = playlist.value
    Column {
        Row(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth()
        ) {
            AudioPlaylist(playlist, currentMediaIndex.value, playByIndex = { play(it) }, isLikedRequester = isLikedRequester, like = like)
        }
        Row(
            modifier = Modifier
                .fillMaxHeight(1f)
                .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                .fillMaxWidth()
        ) {
            if (currentMediaIndex.value >= 0 && plValue is LoadableState.Loaded) {
                PlayerControls(
                    audio = plValue.state[currentMediaIndex.value],
                    playing = playing,
                    maxTimeline = maxTimeline,
                    curTimeline = curTimeline,
                    play = play,
                    pause = pause,
                    previous = previous,
                    next = next,
                    stop = stop,
                    seek = seek
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlaylistControllerPreview() {
    val audios = remember {
        mutableStateOf(
            LoadableState.Loaded(List(8) {
                Audio(it.toLong(), "Test", null, listOf("a", "b"), null)
            })
        )
    }
    val playing = remember {
        mutableStateOf(false)
    }
    val currentMediaIndex = remember {
        mutableStateOf(0)
    }
    val curTimeline = remember {
        mutableStateOf(0L)
    }
    val maxTimeline = remember {
        mutableStateOf(10L)
    }
    val isLiked = MutableLiveData(false)

    AudioTaskTheme {
        PlaylistController(
            currentMediaIndex = currentMediaIndex,
            playlist = audios,
            playing = playing,
            maxTimeline = maxTimeline,
            curTimeline = curTimeline,
            play = { playing.value = true },
            pause = { playing.value = false },
            previous = {},
            next = {},
            stop = {},
            seek = {},
            isLikedRequester = { isLiked },
            like = { a, b -> isLiked.value = b }
        )
    }
}