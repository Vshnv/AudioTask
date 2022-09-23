package com.audifytech.task.audiotask.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.audifytech.task.audiotask.*

const val ACTIONS_PLAY = "player.action.play"
const val ACTIONS_PAUSE = "player.action.pause"
const val ACTIONS_STOP = "player.action.stop"
const val ACTIONS_PREVIOUS = "player.action.previous"
const val ACTIONS_NEXT = "player.action.next"

class PlayerControllerBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        log { "Received ${intent?.action} from External Source" }
        context?.startService(Intent(context, PlayerService::class.java).also {
            it.action = intent?.action
        })
        log { "Started & Forwarded ${intent?.action} to PlayerService" }
    }
}