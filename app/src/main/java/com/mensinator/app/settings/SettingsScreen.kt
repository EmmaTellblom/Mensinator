package com.mensinator.app.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mensinator.app.*
import com.mensinator.app.R
import com.mensinator.app.data.DataSource
import com.mensinator.app.ui.theme.MensinatorTheme
import com.mensinator.app.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.ui.theme.isDarkMode
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

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
private fun SettingSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() },
        style = MaterialTheme.typography.headlineMedium
    )
}

@Preview
@Composable
private fun SettingSectionHeaderPreview() {
    MensinatorTheme {
        SettingSectionHeader(text = stringResource(R.string.colors))
    }
}

@Composable
private fun SettingColorSelection(
    colorSetting: ColorSetting,
    currentColor: Color,
    modifier: Modifier = Modifier,
    onColorChange: (setting: ColorSetting, colorName: String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(colorSetting.stringResId))
        Row {
            Box(
                modifier = Modifier
                    .background(color = currentColor, shape = CircleShape)
                    .size(24.dp)
                    .clickable {
                        onColorChange(colorSetting, "Blue")
                    }
            )
            // TODO color display
        }
    }
}

@Composable
private fun SettingNumberSelection(
    text: String,
    number: Int,
    onNumberChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, modifier = Modifier.weight(1f))
        TextButton(
            onClick = {
                // TODO
            },
            colors = ButtonDefaults.filledTonalButtonColors()
        ) {
            Text("$number ${stringResource(R.string.days)}")
        }
    }
}

@Composable
private fun SettingSwitch(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = {
                // TODO
            }
        )
    }
}

@Composable
private fun SettingLanguagePicker() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        /**
         *  On lower Android versions, there is no possibility to
         *  set app-specific languages.
         *  The device language list is used automatically.
         */
        return
    }

    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.language), modifier = Modifier.weight(1f))
        TextButton(
            onClick = {
                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            },
            colors = ButtonDefaults.filledTonalButtonColors()
        ) {
            Text(stringResource(R.string.change_language))
        }
    }
}

@Preview
@Composable
private fun SettingColorSelectionPreview() {
    MensinatorTheme {
        SettingColorSelection(
            colorSetting = ColorSetting.PERIOD,
            currentColor = Color.Red,
            onColorChange = { _, _ -> }
        )
    }
}

@Composable
fun NewScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val viewState = viewModel.viewState.collectAsStateWithLifecycle().value
    val isDarkMode = isDarkMode()
    LaunchedEffect(isDarkMode) {
        viewModel.updateDarkModeStatus(isDarkMode)
    }
    LaunchedEffect(Unit) {
        viewModel.refreshColors()
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        SettingSectionHeader(text = stringResource(R.string.colors))
        SettingColorSelection(
            colorSetting = ColorSetting.PERIOD,
            currentColor = viewState.periodColor,
            onColorChange = { colorSetting, newColor ->
                viewModel.updateColorSetting(colorSetting, newColor)
            }
        )
        SettingColorSelection(
            colorSetting = ColorSetting.SELECTION,
            currentColor = viewState.selectionColor,
            onColorChange = { colorSetting, newColor ->
                viewModel.updateColorSetting(colorSetting, newColor)
            }
        )
        SettingColorSelection(
            colorSetting = ColorSetting.PERIOD_SELECTION,
            currentColor = viewState.periodSelectionColor,
            onColorChange = { colorSetting, newColor ->
                viewModel.updateColorSetting(colorSetting, newColor)
            }
        )
        SettingColorSelection(
            colorSetting = ColorSetting.EXPECTED_PERIOD,
            currentColor = viewState.expectedPeriodColor,
            onColorChange = { colorSetting, newColor ->
                viewModel.updateColorSetting(colorSetting, newColor)
            }
        )
        SettingColorSelection(
            colorSetting = ColorSetting.OVULATION,
            currentColor = viewState.ovulationColor,
            onColorChange = { colorSetting, newColor ->
                viewModel.updateColorSetting(colorSetting, newColor)
            }
        )
        SettingColorSelection(
            colorSetting = ColorSetting.EXPECTED_OVULATION,
            currentColor = viewState.expectedOvulationColor,
            onColorChange = { colorSetting, newColor ->
                viewModel.updateColorSetting(colorSetting, newColor)
            }
        )

        Spacer(Modifier.height(16.dp))
        SettingSectionHeader(text = stringResource(R.string.reminders))
        SettingNumberSelection(
            text = stringResource(R.string.days_before_reminder),
            number = viewState.daysBeforeReminder,
            onNumberChange = {}
        )

        Spacer(Modifier.height(16.dp))

        SettingSectionHeader(text = stringResource(R.string.other_settings))
        SettingSwitch(
            text = stringResource(R.string.luteal_phase_calculation),
            checked = viewState.lutealPhaseCalculationEnabled
        )
        SettingNumberSelection(text = stringResource(R.string.period_history),
            number = Int.MAX_VALUE,
            onNumberChange = {}
        )
        SettingNumberSelection(text = stringResource(R.string.ovulation_history),
            number = Int.MAX_VALUE,
            onNumberChange = {}
        )
        SettingLanguagePicker()
        SettingSwitch(
            text = stringResource(R.string.cycle_numbers_show),
            checked = false
        )
        SettingSwitch(
            text = stringResource(R.string.screen_protection),
            checked = false
        )

        Spacer(Modifier.height(16.dp))
        SettingSectionHeader(text = stringResource(R.string.data_settings))
        // TODO: saved data + 2 buttons

        Row(horizontalArrangement = Arrangement.Absolute.SpaceAround) {
            TextButton(
                onClick = {
                    // TODO: Show FAQDialog
                }
            ) {
                Text(text = stringResource(R.string.about_app))
            }
            Text(text = stringResource(R.string.about_app)) // TODO: app version
            Text(text = stringResource(R.string.about_app)) // TODO: db version
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewScreenPreview() {
    MensinatorTheme {
        NewScreen()
    }
}

@Composable
fun SettingsScreen(onSwitchProtectionScreen: (Boolean) -> Unit) {
    Log.d("SettingsDialog", "SettingsDialog recomposed")

    val context = LocalContext.current
    val dbHelper: IPeriodDatabaseHelper = koinInject()
    val exportImport: IExportImport = koinInject()

    // Fetch current settings from the database
    val settings by remember { mutableStateOf(dbHelper.getAllSettings()) }

    // State to hold the settings to be saved
    var savedSettings by remember { mutableStateOf(settings) }
    var exportImportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showFAQDialog by remember { mutableStateOf(false) }

    val predefinedReminders = (0..12).map { it.toString() }

    val groupedSettings = savedSettings.groupBy { it.groupId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .displayCutoutExcludingStatusBarsPadding(),
    ) {//we have 2 columns so the scroll animation does get cut by the padding of the second column
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
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
                                Box {
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
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.wrapContentSize()
                                    ) {
                                        // Retrieve the colorMap from DataSource
                                        val colorMap = DataSource(isDarkMode()).colorMap

                                        // Define color categories grouped by hue
                                        val colorCategories =
                                            DataSource(isDarkMode()).colorCategories

                                        // Use 8 rows for the color palettes
                                        Column(
                                            modifier = Modifier.wrapContentSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            colorCategories.forEach { colorGroup ->
                                                Row(
                                                    modifier = Modifier
                                                        .wrapContentSize(),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    colorGroup.forEach { colorName ->
                                                        val colorValue = colorMap[colorName]
                                                        if (colorValue != null) {
                                                            DropdownMenuItem(
                                                                modifier = Modifier
                                                                    .size(50.dp)
                                                                    .clip(RoundedCornerShape(100.dp)),
                                                                onClick = {
                                                                    selectedColorName = colorName
                                                                    expanded = false
                                                                    savedSettings =
                                                                        savedSettings.map {
                                                                            if (it.key == setting.key) it.copy(
                                                                                value = selectedColorName
                                                                            ) else it
                                                                        }
                                                                    // Save the data to the database
                                                                    saveData(
                                                                        savedSettings,
                                                                        dbHelper,
                                                                        context
                                                                    )
                                                                },
                                                                text = {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(25.dp)
                                                                            .clip(
                                                                                RoundedCornerShape(
                                                                                    26.dp
                                                                                )
                                                                            )
                                                                            .background(colorValue)  // Use the color from the map
                                                                    )
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
                                                    saveData(savedSettings, dbHelper, context)
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
                                            if (setting.label == "Protect screen") {
                                                onSwitchProtectionScreen(newValue)
                                            }
                                            isChecked = newValue
                                            savedSettings = savedSettings.map {
                                                if (it.key == setting.key) it.copy(value = if (newValue) "1" else "0") else it
                                            }
                                            saveData(savedSettings, dbHelper, context)
                                        },
                                        colors = SwitchDefaults.colors(
                                        )
                                    )
                                } else if (setting.type == "NO") {
                                    Box(modifier = Modifier.alignByBaseline()) {
                                        var expanded by remember { mutableStateOf(false) }
                                        var selectedReminder by remember {
                                            mutableStateOf(
                                                setting.value
                                            )
                                        }
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
                                                            if (it.key == setting.key) it.copy(
                                                                value = selectedReminder
                                                            ) else it
                                                        }
                                                        saveData(
                                                            savedSettings,
                                                            dbHelper,
                                                            context
                                                        )
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else if (setting.type == "LI" && setting.key == "lang") {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        Box(modifier = Modifier.alignByBaseline()) {
                                            TextButton(onClick = {
                                                val intent =
                                                    Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                                                val uri = Uri.fromParts(
                                                    "package",
                                                    context.packageName,
                                                    null
                                                )
                                                intent.data = uri
                                                context.startActivity(intent)
                                            }) {
                                                Text(stringResource(R.string.change_language))
                                            }
                                        }
                                    } else {
                                        /**
                                         *  On lower Android versions, there is no possibility to
                                         *  set app-specific languages.
                                         *  The device language list is used automatically.
                                         */
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
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.data),
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        showImportDialog = true
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.Import),
                        fontSize = 14.sp
                    )
                }
                VerticalDivider(
                    modifier = Modifier
                        .height(14.dp)
                        .padding(start = 2.dp, end = 2.dp)
                )
                TextButton(
                    onClick = {
                        exportImportDialog = true
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.data_export),
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
    if (exportImportDialog) {
        ExportDialog(
            exportImport = exportImport,
            onDismissRequest = { exportImportDialog = false },
            onExportClick = { exportPath ->
                handleExport(context, exportImport, exportPath)
            }
        )
    }

    // Showing the ExportImportDialog when the user triggers it
    if (showImportDialog) {
        ImportDialog(
            exportImport = exportImport,
            onDismissRequest = { showImportDialog = false },
            onImportClick = { importPath ->
                handleImport(context, exportImport, importPath)
            }
        )
    }

    if (showFAQDialog) {
        FAQDialog(onDismissRequest = { showFAQDialog = false })
    }
}

fun handleExport(context: Context, exportImport: IExportImport, exportPath: String) {
    try {
        exportImport.exportDatabase(exportPath)
        Toast.makeText(context, "Data exported successfully to $exportPath", Toast.LENGTH_SHORT)
            .show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error during export: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("Export", "Export error: ${e.message}", e)
    }
}

fun handleImport(context: Context, exportImport: IExportImport, importPath: String) {
    try {
        exportImport.importDatabase(importPath)
        Toast.makeText(
            context,
            "Data imported successfully from $importPath",
            Toast.LENGTH_SHORT
        ).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error during import: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("Import", "Import error: ${e.message}", e)
    }
}

fun saveData(savedSetting: List<Setting>, dbHelper: IPeriodDatabaseHelper, context: Context) {
    Log.d("SettingsDialog", "Save button clicked")
    savedSetting.forEach { setting: Setting ->
        dbHelper.updateSetting(setting.key, setting.value)
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
        packageInfo.versionName
            ?: throw PackageManager.NameNotFoundException() // Returns the version name, e.g., "1.8.4"
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown" // Fallback if the version name is not found
    }
}