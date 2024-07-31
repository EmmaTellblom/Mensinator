package com.mensinator.app

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.NotificationManagerCompat

@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit
) {
    Log.d("SettingsDialog", "SettingsDialog recomposed")

    val context = LocalContext.current
    val activity = LocalContext.current as? androidx.activity.ComponentActivity
    val dbHelper = remember { PeriodDatabaseHelper(context) }

    // Fetch current settings from the database
    val settings by remember { mutableStateOf(dbHelper.getAllSettings()) }

    // State to hold the settings to be saved
    var savedSettings by remember { mutableStateOf(settings) }

    // Predefined lists
    val predefinedColors = listOf(
        "Red" to Color.Red,
        "Green" to Color.Green,
        "Blue" to Color.Blue,
        "Yellow" to Color.Yellow,
        "Cyan" to Color.Cyan,
        "Magenta" to Color.Magenta,
        "Black" to Color.Black,
        "White" to Color.White,
        "Dark Gray" to Color.DarkGray,
        "Light Gray" to Color.LightGray
    )

    val predefinedReminders = (0..10).map { it.toString() }

    val groupedSettings = savedSettings.groupBy { it.groupId }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "App Settings", fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                groupedSettings.forEach { (groupId, settingsInGroup) ->
                    when (groupId) {
                        1 -> {
                            Text(
                                text = "Colors",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            settingsInGroup.forEach { setting ->
                                var expanded by remember { mutableStateOf(false) }
                                var selectedColorName by remember { mutableStateOf(setting.value) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = setting.label,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f).alignByBaseline()
                                    )
                                    Box(modifier = Modifier.alignByBaseline()) {
                                        TextButton(onClick = { expanded = !expanded }) {
                                            Text(selectedColorName)
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            predefinedColors.forEach { (name, _) ->
                                                DropdownMenuItem(
                                                    text = { Text(name) },
                                                    onClick = {
                                                        selectedColorName = name
                                                        savedSettings = savedSettings.map {
                                                            if (it.key == setting.key) it.copy(value = selectedColorName) else it
                                                        }
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Text(
                                text = "Reminders",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            settingsInGroup.forEach { setting ->
                                var expanded by remember { mutableStateOf(false) }
                                var selectedReminder by remember { mutableStateOf(setting.value) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = setting.label,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f).alignByBaseline()
                                    )
                                    Box(modifier = Modifier.alignByBaseline()) {
                                        TextButton(onClick = { expanded = !expanded }) {
                                            Text(selectedReminder)
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            predefinedReminders.forEach { reminder ->
                                                DropdownMenuItem(
                                                    text = { Text(reminder) },
                                                    onClick = {
                                                        selectedReminder = reminder
                                                        savedSettings = savedSettings.map {
                                                            if (it.key == setting.key) it.copy(value = selectedReminder) else it
                                                        }
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = "Other settings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            settingsInGroup.forEach { setting ->
                                var isChecked by remember { mutableStateOf(setting.value == "1") }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = setting.label,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (setting.type == "SW") {
                                        Switch(
                                            checked = isChecked,
                                            onCheckedChange = { newValue ->
                                                isChecked = newValue
                                                savedSettings = savedSettings.map {
                                                    if (it.key == setting.key) it.copy(value = if (newValue) "1" else "0") else it
                                                }
                                            }
                                        )
                                    } else if (setting.type == "NO") {
                                        Box(modifier = Modifier.alignByBaseline()) {
                                            var expanded by remember { mutableStateOf(false) }
                                            var selectedReminder by remember { mutableStateOf(setting.value) }
                                            TextButton(onClick = { expanded = !expanded }) {
                                                Text(selectedReminder)
                                            }
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                predefinedReminders.forEach { reminder ->
                                                    DropdownMenuItem(
                                                        text = { Text(reminder) },
                                                        onClick = {
                                                            selectedReminder = reminder
                                                            savedSettings = savedSettings.map {
                                                                if (it.key == setting.key) it.copy(value = selectedReminder) else it
                                                            }
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                Log.d("SettingsDialog", "Save button clicked")

                savedSettings.forEach { setting ->
                    dbHelper.updateSetting(setting.key, setting.value)
                    Log.d("SettingsDialog", "Updated setting ${setting.key} to ${setting.value}")
                    if (setting.key == "reminder_days" && setting.value.toInt() > 0) {
                        Log.d("SettingsDialog", "Reminder days set and value > 0")
                        if (activity != null) {
                            if (!areNotificationsEnabled(context)) {
                                Log.d("SettingsDialog", "Notifications are not enabled")
                                openNotificationSettings(context)
                            }
                        }
                    }
                }
                onDismissRequest()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Close")
            }
        },
        modifier = Modifier.width(LocalConfiguration.current.screenWidthDp.dp * 0.9f)
    )
}

fun areNotificationsEnabled(context: Context): Boolean {
    val notificationManager = NotificationManagerCompat.from(context)
    return notificationManager.areNotificationsEnabled()
}

fun openNotificationSettings(context: Context) {
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}
