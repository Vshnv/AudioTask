package com.audifytech.task.audiotask

import android.os.Build
import android.util.Log

inline fun Any.log(messageGenerator: () -> String) {
    if (BuildConfig.DEBUG) {
        Log.d(this.javaClass.simpleName, messageGenerator())
    }
}