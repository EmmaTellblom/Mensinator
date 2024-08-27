package com.mensinator.app.navigation

import androidx.annotation.StringRes

data class BarItem(
    @StringRes val title: Int,
    val imageSelected: Int,
    val imageUnSelected: Int
)
