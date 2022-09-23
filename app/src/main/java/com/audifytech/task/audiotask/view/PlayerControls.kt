package com.audifytech.task.audiotask.view

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audifytech.task.audiotask.R
import com.audifytech.task.audiotask.model.data.Audio
import com.audifytech.task.audiotask.ui.theme.AudioTaskTheme

@Composable
fun PlayerControls(
    audio: Audio,
    playing: State<Boolean>,
    maxTimeline: State<Long>,
    curTimeline: State<Long>,
    play: (Int?) -> Unit,
    pause: () -> Unit,
    next: () -> Unit,
    previous: () -> Unit,
    stop: () -> Unit,
    seek: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            .padding(top = 0.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DragPillIcon()
        Row(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icons_music),
                    contentDescription = "Default Icon for Music Items"
                )
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(modifier = Modifier.fillMaxWidth(), text = audio.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = audio.artists.joinToString(separator = ", "),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                PlayerButton(size = 40.dp, onClick = previous) {
                    Icon(
                        painter = painterResource(id = R.drawable.icons_previous),
                        contentDescription = "Play Button Icon"
                    )
                }
                if (playing.value) {
                    PlayerButton(size = 50.dp, onClick = { pause() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icons_pause),
                            contentDescription = "Default Icon for Music Items"
                        )
                    }
                } else {
                    PlayerButton(size = 50.dp, onClick = { play(null) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icons_play),
                            contentDescription = "Default Icon for Music Items"
                        )
                    }
                }
                PlayerButton(size = 40.dp, onClick = next) {
                    Icon(
                        painter = painterResource(id = R.drawable.icons_next),
                        contentDescription = "Default Icon for Music Items"
                    )
                }
                PlayerButton(onClick = stop) {
                    Icon(
                        painter = painterResource(id = R.drawable.icons_stop),
                        contentDescription = "Default Icon for Music Items"
                    )
                }
            }
        }
        Slider(
            modifier = Modifier.fillMaxWidth(0.9f),
            value = curTimeline.value / 1000f,
            valueRange = (0f..(maxTimeline.value / 1000).toFloat()),
            onValueChange = { seek((it * 1000).toLong()) })
    }

}

@Composable
fun DragPillIcon() {
    Row(
        modifier = Modifier
            .height(5.dp)
            .width(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {}
}

@Composable
fun PlayerButton(onClick: () -> Unit = { }, size: Dp = 30.dp, content: @Composable () -> Unit) {
    Button(
        modifier = Modifier
            .size(size)
            .aspectRatio(1f), contentPadding = PaddingValues(0.dp), onClick = onClick
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerControlsPreview() {
    val playing = remember {
        mutableStateOf(false)
    }
    val curTimeline = remember {
        mutableStateOf(2L)
    }
    val maxTimeline = remember {
        mutableStateOf(10L)
    }
    AudioTaskTheme {
        Row(modifier = Modifier.height(200.dp)) {
            PlayerControls(
                Audio(0, "Test", null, listOf("a", "b"), null),
                playing = playing,
                maxTimeline = maxTimeline,
                curTimeline = curTimeline,
                play = { playing.value = true },
                pause = { playing.value = false },
                next = {},
                previous = {},
                stop = {},
                seek = {})
        }
    }
}