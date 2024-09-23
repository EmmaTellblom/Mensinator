package com.mensinator.app

import android.content.Context

open class Prediction(context: Context) {

    val dbHelper = PeriodDatabaseHelper(context)
    val calcHelper = Calculations(context)
}