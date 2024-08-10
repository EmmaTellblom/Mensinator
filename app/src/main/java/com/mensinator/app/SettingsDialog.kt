package com.mensinator.app

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.LocaleListCompat

//Maps Database keys to res/strings.xml for multilanguage support
object ResourceMapper {
    //maps res strings xml file to db keys
    private val resourceMap = mapOf(
        //settings
        "app_settings" to R.string.app_settings,
        "period_color" to R.string.period_color,
        "selection_color" to R.string.selection_color,
        "period_selection_color" to R.string.period_selection_color,
        "expected_period_color" to R.string.expected_period_color,
        "ovulation_color" to R.string.ovulation_color,
        "expected_ovulation_color" to R.string.expected_ovulation_color,
        "reminders" to R.string.reminders,
        "reminder_days" to R.string.days_before_reminder,
        "other_settings" to R.string.other_settings,
        "luteal_period_calculation" to R.string.luteal_phase_calculation,
        "period_history" to R.string.period_history,
        "ovulation_history" to R.string.ovulation_history,
        "lang" to R.string.language,
        "cycle_numbers_show" to R.string.cycle_numbers_show,
        "close" to R.string.close,
        "save" to R.string.save,
        "Heavy_Flow" to R.string.heavy,
        "Medium_Flow" to R.string.medium,
        "Light_Flow" to R.string.light,
        // colors
        "Red" to R.string.color_red,
        "Green" to R.string.color_green,
        "Blue" to R.string.color_blue,
        "Yellow" to R.string.color_yellow,
        "Cyan" to R.string.color_cyan,
        "Magenta" to R.string.color_magenta,
        "Black" to R.string.color_black,
        "White" to R.string.color_white,
        "DarkGray" to R.string.color_darkgray,
        "Grey" to R.string.color_grey,

        )

    fun getStringResourceId(key: String): Int? {
        return resourceMap[key]
    }
}

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

    // Here is available languages of the app
    // When more languages have been translated, add them here
    val predefinedLang = mapOf(
        "English" to "en",
        "Svenska" to "sv",
        "Tamil" to "ta",
        "Romanian" to "ro",
        "Hindi" to "hi"
        /*"Chinese" to "zh",
        "Spanish" to "es",
        "Bengali" to "bn",
        "Portuguese" to "pt",
        "Russian" to "ru",
        "Japanese" to "ja",
        "Western Punjabi" to "pa",
        "Marathi" to "mr",
        "Telugu" to "te",
        "Wu Chinese" to "wuu",
        "Turkish" to "tr",
        "Korean" to "ko",
        "French" to "fr",
        "German" to "de",
        "Vietnamese" to "vi",
        "Urdu" to "ur",
        "Cantonese" to "yue"*/

    )

    val predefinedReminders = (0..12).map { it.toString() }

    val groupedSettings = savedSettings.groupBy { it.groupId }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.app_settings), fontSize = 20.sp)
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
                                text = stringResource(id = R.string.colors),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            settingsInGroup.forEach { setting ->
                                var expanded by remember { mutableStateOf(false) }
                                var selectedColorName by remember { mutableStateOf(setting.value) }
                                val settingsKey = ResourceMapper.getStringResourceId(setting.key)
                                val selectedColorKey = ResourceMapper.getStringResourceId(selectedColorName)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = settingsKey?.let { stringResource(id = it) } ?:"Not found",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .alignByBaseline()
                                    )
                                    Box(modifier = Modifier.alignByBaseline()) {
                                        TextButton(onClick = { expanded = !expanded }) {
                                            Text(selectedColorKey?.let { stringResource(id = it) } ?:"Not found")
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            predefinedColors.forEach { (name, _) ->
                                                val colors = ResourceMapper.getStringResourceId(name)
                                                DropdownMenuItem(
                                                    text = { Text(colors?.let { stringResource(id = it) } ?:"Not found") },
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
                                text = stringResource(id = R.string.reminders),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            settingsInGroup.forEach { setting ->
                                var expanded by remember { mutableStateOf(false) }
                                var selectedReminder by remember { mutableStateOf(setting.value) }
                                val settingsKey = ResourceMapper.getStringResourceId(setting.key)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = settingsKey?.let { stringResource(id = it) } ?: setting.label,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .alignByBaseline()
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
                                text = stringResource(id = R.string.other_settings),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            settingsInGroup.forEach { setting ->
                                var isChecked by remember { mutableStateOf(setting.value == "1") }
                                val settingsKey = ResourceMapper.getStringResourceId(setting.key)


                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = settingsKey?.let { stringResource(id = it) } ?: setting.label,
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
                                    else if (setting.type == "LI" && setting.key == "lang") {
                                        var expanded by remember { mutableStateOf(false) }
                                        var selectedLang by remember {
                                            mutableStateOf(
                                                predefinedLang.entries.find { it.value == setting.value }?.key
                                                    ?: throw IllegalStateException("Locale code ${setting.value} not found in predefined languages.")
                                            )
                                        }

                                        Box(modifier = Modifier.alignByBaseline()) {
                                            TextButton(onClick = { expanded = !expanded }) {
                                                Text(selectedLang)
                                            }
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                predefinedLang.forEach { (name, code) ->
                                                    DropdownMenuItem(
                                                        text = { Text(name) },
                                                        onClick = {
                                                            selectedLang = name
                                                            savedSettings = savedSettings.map {
                                                                if (it.key == setting.key) it.copy(value = code) else it
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
                    // Update the application locale if the language setting has changed
                    if (setting.key == "lang") {
                        val newLocale = setting.value
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(newLocale)
                        )
                    }
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
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.close))
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
