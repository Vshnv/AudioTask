package com.audifytech.task.audiotask.model.data

sealed class LoadableState<T> {
    class Loaded<T>(val state: T): LoadableState<T>()
    class Loading<T> : LoadableState<T>()
    class Uninitialized<T> : LoadableState<T>()
}