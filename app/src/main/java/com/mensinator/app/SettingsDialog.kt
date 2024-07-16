package com.mensinator.app

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

@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) } // Use remember to avoid reinitialization

    // Fetch current settings from the database
    val settings by remember { mutableStateOf(dbHelper.getAllSettings()) } // Get all settings once

    // State to hold the settings to be saved
    var savedSettings by remember { mutableStateOf(settings) }

    // Predefined list of colors
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

    // Predefined list of reminders
    val predefinedReminders = (0..10).map { it.toString() }

    // Group settings by COLUMN_SETTING_GROUP_ID
    val groupedSettings = savedSettings.groupBy { it.groupId }

    // Create a scrollable column for the dialog content
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
                    .verticalScroll(rememberScrollState())  // Make the column scrollable
            ) {
                // Iterate through groups to display appropriate content
                groupedSettings.forEach { (groupId, settingsInGroup) ->
                    when (groupId) {
                        1 -> {
                            // Colors group header
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
                                    verticalAlignment = Alignment.CenterVertically // Align items vertically
                                ) {
                                    Text(
                                        text = setting.label,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f).alignByBaseline() // Align by baseline
                                    )
                                    Box(modifier = Modifier.alignByBaseline()) { // Align by baseline
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
                            // Reminders group header
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
                                    verticalAlignment = Alignment.CenterVertically // Align items vertically
                                ) {
                                    Text(
                                        text = setting.label,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f).alignByBaseline() // Align by baseline
                                    )
                                    Box(modifier = Modifier.alignByBaseline()) { // Align by baseline
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
                            // Default case for other groups
                            settingsInGroup.forEach { setting ->
                                Text(
                                    text = "${setting.label}: ${setting.value}",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Save settings to the database
                savedSettings.forEach { setting ->
                    dbHelper.updateSetting(setting.key, setting.value)
                }
                onDismissRequest()  // Close the dialog
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Close")
            }
        },
        // To adjust the width of the AlertDialog based on screen size
        modifier = Modifier.width(LocalConfiguration.current.screenWidthDp.dp * 0.9f) // Adjust width to 90% of screen width
    )
}