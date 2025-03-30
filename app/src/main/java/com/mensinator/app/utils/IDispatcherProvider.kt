package com.mensinator.app.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Suppress("unused", "PropertyName")
interface IDispatcherProvider {
    val Main: CoroutineDispatcher
        get() = Dispatchers.Main
    val Default
        get() = Dispatchers.Default
    val IO
        get() = Dispatchers.IO
    val Unconfined
        get() = Dispatchers.Unconfined
}

class DefaultDispatcherProvider : IDispatcherProvider