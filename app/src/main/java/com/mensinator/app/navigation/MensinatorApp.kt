package com.mensinator.app.navigation

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mensinator.app.*
import com.mensinator.app.R
import org.koin.compose.koinInject

enum class Screen(@StringRes val titleRes: Int) {
    Calendar(R.string.calendar_title),
    Symptoms(R.string.symptoms_page),
    Statistic(R.string.statistics_title),
    Settings(R.string.settings_page)
}

/**
 * The displayCutout insets are necessary for landscape usage, so that the UI is not behind the camera.
 *
 * At MensinatorTopBar, the statusBars insets are used. But as it's only a siblings view,
 * it is not treated as consumed. Thus, on the individual screens, we have to exclude it.
 */
@Composable
fun Modifier.displayCutoutExcludingStatusBarsPadding() =
    windowInsetsPadding(WindowInsets.displayCutout.exclude(WindowInsets.statusBars))

@Composable
fun MensinatorApp(
    navController: NavHostController = rememberNavController(),
    onScreenProtectionChanged: (Boolean) -> Unit?,
) {
    val dbHelper: IPeriodDatabaseHelper = koinInject()
    // If protectScreen is 1, it should protect the screen
    // If protectScreen is 0, should not protect screen(allows prints and screen visibility in recent apps)
    val protectScreen = dbHelper.getSettingByKey("screen_protection")?.value?.toIntOrNull() ?: 1
    Log.d("screenProtectionUI", "protect screen value $protectScreen")
    onScreenProtectionChanged(protectScreen != 0)

//    var nextPeriodStartCalculated by remember { mutableStateOf("Not enough data") }
//    var nextOvulationCalculated by remember { mutableStateOf("Not enough data") }
//    var follicleGrowthDays by remember { mutableStateOf("0") }

    val showCreateSymptom = rememberSaveable { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.Calendar.name
    )

    Scaffold(
        floatingActionButton = {
            if (currentScreen == Screen.Symptoms) {
                FloatingActionButton(
                    onClick = { showCreateSymptom.value = true },
                    shape = CircleShape,
                    modifier = Modifier
                        .displayCutoutPadding()
                        .padding(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.delete_button)
                    )
                }
            }
        },
        topBar = { MensinatorTopBar(currentScreen) },
        bottomBar = {
            val barItems = listOf(
                BarItem(
                    screen = Screen.Calendar,
                    R.drawable.baseline_calendar_month_24,
                    R.drawable.baseline_calendar_month_24 //here you can add not_field icon if you want. when its not selected
                ),
                BarItem(
                    screen = Screen.Statistic,
                    R.drawable.outline_bar_chart_24,
                    R.drawable.outline_bar_chart_24
                ),
                BarItem(
                    screen = Screen.Symptoms,
                    R.drawable.baseline_bloodtype_24,
                    R.drawable.baseline_bloodtype_24
                ),
                BarItem(
                    screen = Screen.Settings,
                    R.drawable.settings_24px,
                    R.drawable.settings_24px
                ),
            )
            NavigationBar {
                barItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentScreen == item.screen,
                        onClick = {
                            navController.navigate(item.screen.name) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val image = if (currentScreen == item.screen) {
                                item.imageSelected
                            } else {
                                item.imageUnSelected
                            }
                            Icon(
                                imageVector = ImageVector.vectorResource(image),
                                contentDescription = stringResource(item.screen.titleRes)
                            )
                        }
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.name,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(50)) },
            exitTransition = { fadeOut(animationSpec = tween(50)) },
        ) {//create a new file for every page and pass it inside the composable
            composable(route = Screen.Calendar.name) {
                CalendarScreen()
            }
            composable(route = Screen.Statistic.name) {
                StatisticsScreen()
            }
            composable(route = Screen.Symptoms.name) {
                ManageSymptomScreen(showCreateSymptom)
            }
            composable(route = Screen.Settings.name) {
                SettingsScreen(onSwitchProtectionScreen = { newValue ->
                    onScreenProtectionChanged(newValue)
                })
            }
        }
    }

}

