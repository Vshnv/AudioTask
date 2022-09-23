package com.audifytech.task.audiotask.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.audifytech.task.audiotask.R
import com.audifytech.task.audiotask.model.data.Audio
import com.audifytech.task.audiotask.model.data.LoadableState
import com.audifytech.task.audiotask.ui.theme.AudioTaskTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun AudioPlaylist(
    playListState: State<LoadableState<List<Audio>>>,
    currentIndex: Int,
    playByIndex: (Int) -> Unit,
    isLikedRequester: (Long) -> LiveData<Boolean>,
    like: (Long, Boolean) -> Unit
) {
    val pl = playListState.value
    Column {
        when (pl) {
            is LoadableState.Uninitialized -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Initializing")
                }
            }
            is LoadableState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            is LoadableState.Loaded -> {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    pl.state.forEachIndexed { index, audio ->
                        PlaylistItem(
                            audio = audio,
                            play = { playByIndex(index) },
                            active = index == currentIndex,
                            isLiked = isLikedRequester(audio.id),
                            like = { like(audio.id, it) }
                        )
                        Divider(modifier = Modifier.fillMaxWidth(0.9f))
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistItem(
    audio: Audio,
    play: () -> Unit,
    active: Boolean = false,
    isLiked: LiveData<Boolean>,
    like: (Boolean) -> Unit
) {
    val likeState = isLiked.observeAsState()
    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .padding(5.dp)
            .clickable { play() }.let {
                if (active) {
                    it
                        .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(20.dp))
                } else {
                    it
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icons_music),
            contentDescription = "Default Icon for Music Items"
        )
        Column(modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth(0.9f)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = audio.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = audio.artists.joinToString(separator = ","),
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.weight(1f))
        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(end = 10.dp)
                .clickable {
                    GlobalScope.launch {
                        like(likeState.value?.not() ?: true)
                    }
                },
            painter = painterResource(id = R.drawable.icon_like),
            tint = if (likeState.value == true) Color.Red else Color.Gray,
            contentDescription = "Icon to indicate Liked Audios"
        )

    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistPreview() {
    val audios = remember {
        mutableStateOf(
            LoadableState.Loaded(
                listOf(
                    Audio(0, "Test", null, listOf("a", "b"), null),
                    Audio(1, "Test1", null, listOf("a", "b"), null),
                    Audio(2, "Test2", null, listOf("a", "b"), null)
                )
            )
        )
    }
    val isLiked = MutableLiveData(false)
    AudioTaskTheme {
        AudioPlaylist(
            audios,
            currentIndex = 0,
            isLikedRequester = { isLiked },
            like = { a, b -> isLiked.value = b },
            playByIndex = {})
    }
}