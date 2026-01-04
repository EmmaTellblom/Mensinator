package com.mensinator.app.widgets

import kotlinx.coroutines.flow.MutableSharedFlow

// Provide reset signal for widget
object MidnightTrigger {
    val midnightTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }
}