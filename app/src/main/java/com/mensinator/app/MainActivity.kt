package com.mensinator.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mensinator.app.NotificationChannelConstants.channelDescription
import com.mensinator.app.NotificationChannelConstants.channelId
import com.mensinator.app.NotificationChannelConstants.channelName
import com.mensinator.app.ui.navigation.MensinatorApp
import com.mensinator.app.ui.theme.MensinatorTheme
import com.mensinator.app.widgets.MidnightTrigger
import com.mensinator.app.widgets.WidgetInstances
import kotlinx.coroutines.launch
import org.koin.androidx.compose.KoinAndroidContext
import androidx.glance.appwidget.updateAll

@Suppress("ConstPropertyName")
object NotificationChannelConstants {
    const val channelId = "1"
    const val channelName = "Mensinator"
    const val channelDescription = "Reminders about upcoming periods"
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MensinatorTheme {
                KoinAndroidContext {
                    MensinatorApp(onScreenProtectionChanged = ::handleScreenProtection)
                }
            }
        }
        createNotificationChannel(this)
        updateWidgetsOnAppStart()
    }

    private fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun updateWidgetsOnAppStart() {
        lifecycleScope.launch {
            // Emit to midnight trigger to force widget data refresh
            MidnightTrigger.midnightTrigger.emit(Unit)
            // Update all widgets
            WidgetInstances.forEach { receiver ->
                receiver.glanceAppWidget.updateAll(this@MainActivity)
            }
        }
    }

    private fun handleScreenProtection(isScreenProtectionEnabled: Boolean) {
        Log.d("screenProtectionUI", "protect screen value $isScreenProtectionEnabled")
        // Sets the flags for screen protection if
        // isScreenProtectionEnabled == true
        // If isScreenProtectionEnabled == false it removes the flags
        if (isScreenProtectionEnabled) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}