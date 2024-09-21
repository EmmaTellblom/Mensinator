package com.mensinator.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.LocaleListCompat
import com.mensinator.app.data.DataSource
import com.mensinator.app.ui.theme.isDarkMode
import androidx.compose.ui.platform.LocalConfiguration

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
        "screen_protection" to R.string.screen_protection,
        // colors
//        "Red" to R.string.color_red,
//        "Green" to R.string.color_green,
//        "Blue" to R.string.color_blue,
//        "Yellow" to R.string.color_yellow,
//        "Cyan" to R.string.color_cyan,
//        "Magenta" to R.string.color_magenta,
//        "Black" to R.string.color_black,
//        "White" to R.string.color_white,
//        "DarkGray" to R.string.color_darkgray,
//        "LightGray" to R.string.color_gray,
    )

    fun getStringResourceId(key: String): Int? {
        return resourceMap[key]
    }
}



@Composable
fun SettingsDialog() {
    Log.d("SettingsDialog", "SettingsDialog recomposed")

    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }

    // Fetch current settings from the database
    val settings by remember { mutableStateOf(dbHelper.getAllSettings()) }

    // State to hold the settings to be saved
    var savedSettings by remember { mutableStateOf(settings) }
    var showExportImportDialog by remember { mutableStateOf(false) }
    var showFAQDialog by remember { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val menuHeight = screenHeight * 0.8f // 80% of the screen height

    // Here is available languages of the app
    // When more languages have been translated, add them here
    val predefinedLang = mapOf(
        "English" to "en",
        "Swedish" to "sv",
        "Tamil" to "ta",
        "Romanian" to "ro",
        "Hindi" to "hi",
        "Bengali" to "bn",
        "Spanish" to "es",
        "French" to "fr",
        "Polish" to "pl",
        "Slovenian" to "sl"
        /*"Chinese" to "zh",
        "Portuguese" to "pt",
        "Russian" to "ru",
        "Japanese" to "ja",
        "Western Punjabi" to "pa",
        "Marathi" to "mr",
        "Telugu" to "te",
        "Wu Chinese" to "wuu",
        "Turkish" to "tr",
        "Korean" to "ko",
        "German" to "de",
        "Vietnamese" to "vi",
        "Urdu" to "ur",
        "Cantonese" to "yue"*/

    )

    val predefinedReminders = (0..12).map { it.toString() }

    val groupedSettings = savedSettings.groupBy { it.groupId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {//we have 2 columns so the scroll animation does get cut by the padding of the second column
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(id = R.string.app_settings),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            groupedSettings.forEach { (groupId, settingsInGroup) ->
                when (groupId) {
                    1 -> {
                        Text(
                            text = stringResource(id = R.string.colors),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        settingsInGroup.forEach { setting ->
                            var expanded by remember { mutableStateOf(false) }
                            var selectedColorName by remember { mutableStateOf(setting.value) }
                            val settingsKey = ResourceMapper.getStringResourceId(setting.key)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = settingsKey?.let { stringResource(id = it) }
                                        ?: "Not found",
                                    fontSize = 14.sp,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Box{
                                    Card(
                                        modifier = Modifier
                                            .clickable { }
                                            .clip(RoundedCornerShape(26.dp)),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent,
                                        ),
                                        onClick = { expanded = true }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(RoundedCornerShape(26.dp))
                                                    .background(
                                                        selectedColorName.let {
                                                            DataSource(isDarkMode()).colorMap[selectedColorName]
                                                        }
                                                            ?: Color.Gray
                                                    ),
                                            )
                                            Icon(
                                                painter = painterResource(id = R.drawable.keyboard_arrow_down_24px),
                                                contentDescription = stringResource(
                                                    id =
                                                    R.string.selection_color
                                                ),
                                                modifier = Modifier.wrapContentSize()
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(menuHeight),
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {

                                        DataSource(isDarkMode()).colorMap.forEach { (name, colorValue) ->
                                            //val colors = ResourceMapper.getStringResourceId(name)
                                            DropdownMenuItem(
                                                text = {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(25.dp)
                                                            .clip(RoundedCornerShape(26.dp))
                                                            .background(colorValue),  // Use the color from the map
                                                    )
                                                },
                                                onClick = {
                                                    selectedColorName = name
                                                    savedSettings = savedSettings.map {
                                                        if (it.key == setting.key) it.copy(value = selectedColorName) else it
                                                    }
                                                    saveData(savedSettings,dbHelper,context)
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
                                    text = settingsKey?.let { stringResource(id = it) }
                                        ?: setting.label,
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
                                                    saveData(savedSettings,dbHelper,context)
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
                                    text = settingsKey?.let { stringResource(id = it) }
                                        ?: setting.label,
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
                                            saveData(savedSettings,dbHelper,context)
                                        },
                                        colors = SwitchDefaults.colors(
                                        )
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
                                                        saveData(savedSettings,dbHelper,context)
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else if (setting.type == "LI" && setting.key == "lang") {
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
                                                        saveData(savedSettings,dbHelper,context)
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
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.data_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.import_export_data),
                    fontSize = 14.sp,
                )
                TextButton(
                    onClick = {
                        showExportImportDialog = true
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.import_export_data),
                        fontSize = 14.sp
                    )
                }
            }
            val contextApp = LocalContext.current
            val appVersion = getAppVersion(contextApp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { showFAQDialog = true }) {
                    Text(text = stringResource(id = R.string.about_app), fontSize = 12.sp)
                }
                //Spacer(modifier = Modifier.width(8.dp)) // Space between text elements
                Spacer(modifier = Modifier.width(8.dp)) // Space between text elements
                Text(
                    text = "    |   App Version: $appVersion   |   DB-version: ${dbHelper.getDBVersion()}",
                    fontSize = 10.sp
                )
            }

        }
    }
    // Showing the ExportImportDialog when the user triggers it
    if (showExportImportDialog) {
        ExportImportDialog(
            onDismissRequest = { showExportImportDialog = false },
            onExportClick = { exportPath ->
                handleExport(context, exportPath)
            },
            onImportClick = { importPath ->
                handleImport(context, importPath)
            }
        )
    }

    if (showFAQDialog) {
        FAQDialog(onDismissRequest = { showFAQDialog = false })
    }
}

fun handleExport(context: Context, exportPath: String) {
    try {
        val exportImport = ExportImport()
        exportImport.exportDatabase(context, exportPath)
        Toast.makeText(context, "Data exported successfully to $exportPath", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error during export: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("Export", "Export error: ${e.message}", e)
    }
}

fun handleImport(context: Context, importPath: String) {
    try {
        val exportImport = ExportImport()
        exportImport.importDatabase(context, importPath)
        Toast.makeText(context, "Data imported successfully from $importPath", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error during import: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("Import", "Import error: ${e.message}", e)
    }
}

fun saveData(savedSetting: List<Setting>,dbHelper: PeriodDatabaseHelper,context: Context) {
    Log.d("SettingsDialog", "Save button clicked")
    savedSetting.forEach { setting ->
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
            if (!areNotificationsEnabled(context)) {
                Log.d("SettingsDialog", "Notifications are not enabled")
                openNotificationSettings(context)
            }
        }
    }

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

fun getAppVersion(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName // Returns the version name, e.g., "1.8.4"
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown" // Fallback if the version name is not found
    }
}