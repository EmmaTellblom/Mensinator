package com.mensinator.app.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
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
import com.mensinator.app.R
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.calendar.CalendarScreen
import com.mensinator.app.settings.SettingsScreen
import com.mensinator.app.statistics.StatisticsScreen
import com.mensinator.app.symptoms.ManageSymptomScreen
import com.mensinator.app.ui.theme.UiConstants
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MensinatorApp(
    navController: NavHostController = rememberNavController(),
    onScreenProtectionChanged: (Boolean) -> Unit?,
) {
    val dbHelper: IPeriodDatabaseHelper = koinInject()

    LaunchedEffect(Unit) {
        launch {
            // If protectScreen is 1, it should protect the screen
            // If protectScreen is 0, should not protect screen (allows screenshots and screen visibility in recent apps)
            val protectScreen = (dbHelper.getSettingByKey("screen_protection")?.value?.toIntOrNull() ?: 1) == 1
            onScreenProtectionChanged(protectScreen)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.Calendar.name
    )

    val activity = LocalActivity.current ?: return
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isMediumExpandedWWSC by remember(windowSizeClass) {
        derivedStateOf {
            windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
        }
    }

    MainScaffold(
        currentScreen = currentScreen,
        navController = navController,
        onScreenProtectionChanged = onScreenProtectionChanged,
        isMediumExpandedWWSC = isMediumExpandedWWSC,
        onItemClick = { item ->
            navController.navigate(item.screen.name) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}

@Composable
private fun MainScaffold(
    currentScreen: Screen,
    navController: NavHostController,
    onScreenProtectionChanged: (Boolean) -> Unit?,
    isMediumExpandedWWSC: Boolean,
    onItemClick: (NavigationItem) -> Unit
) {
    Row {
        if (isMediumExpandedWWSC) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)),
                    verticalArrangement = Arrangement.Center
                ) {
                    navigationItems.forEach { item ->
                        NavigationRailItem(
                            selected = currentScreen == item.screen,
                            onClick = { onItemClick(item) },
                            icon = { NavigationItemIcon(currentScreen, item) },
                        )
                    }
                }
            }
        }

        Scaffold(
            bottomBar = {
                if (!isMediumExpandedWWSC) {
                    NavigationBar(
                        //modifier = Modifier.consumeWindowInsets(NavigationBarDefaults.windowInsets)
                    ) {
                        navigationItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentScreen == item.screen,
                                onClick = { onItemClick(item) },
                                icon = { NavigationItemIcon(currentScreen, item) }
                            )
                        }
                    }
                }
            },
        ) { rootPaddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Calendar.name,
                modifier = Modifier.padding(rootPaddingValues),
                enterTransition = { fadeIn(animationSpec = tween(50)) },
                exitTransition = { fadeOut(animationSpec = tween(50)) },
            ) {
                composable(route = Screen.Calendar.name) {
                    // Adapted from https://stackoverflow.com/a/71191082/3991578
                    val (toolbarOnClick, setToolbarOnClick) = remember { mutableStateOf<(() -> Unit)?>(null) }
                    Scaffold(
                        topBar = {
                            MensinatorTopBar(
                                titleStringId = currentScreen.titleRes,
                                onTitleClick = toolbarOnClick
                            )
                        },
                        contentWindowInsets = WindowInsets(0.dp),
                    ) { topBarPadding ->
                        CalendarScreen(
                            modifier = Modifier.padding(topBarPadding),
                            setToolbarOnClick = setToolbarOnClick
                        )
                    }
                }
                composable(route = Screen.Statistic.name) {
                    Scaffold(
                        topBar = { MensinatorTopBar(currentScreen.titleRes) },
                        contentWindowInsets = WindowInsets(0.dp),
                    ) { topBarPadding ->
                        StatisticsScreen(modifier = Modifier.padding(topBarPadding))
                    }
                }
                composable(route = Screen.Symptoms.name) {
                    // Adapted from https://stackoverflow.com/a/71191082/3991578
                    // Needed so that the action button can cause the dialog to be shown
                    val (fabOnClick, setFabOnClick) = remember { mutableStateOf<(() -> Unit)?>(null) }
                    Scaffold(
                        floatingActionButton = {
                            if (currentScreen == Screen.Symptoms) {
                                FloatingActionButton(
                                    onClick = { fabOnClick?.invoke() },
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .displayCutoutPadding()
                                        .size(UiConstants.floatingActionButtonSize)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(R.string.delete_button)
                                    )
                                }
                            }
                        },
                        topBar = { MensinatorTopBar(currentScreen.titleRes) },
                        contentWindowInsets = WindowInsets(0.dp),
                    ) { topBarPadding ->
                        ManageSymptomScreen(
                            modifier = Modifier.padding(topBarPadding),
                            setFabOnClick = setFabOnClick
                        )
                    }
                }
                composable(route = Screen.Settings.name) {
                    Scaffold(
                        topBar = { MensinatorTopBar(currentScreen.titleRes) },
                        contentWindowInsets = WindowInsets(0.dp),
                    ) { topBarPadding ->
                        Column {
                            SettingsScreen(
                                onSwitchProtectionScreen = { newValue ->
                                    onScreenProtectionChanged(newValue)
                                },
                                modifier = Modifier.padding(topBarPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationItemIcon(
    currentScreen: Screen,
    item: NavigationItem
) {
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

private val navigationItems = listOf(
    NavigationItem(
        screen = Screen.Calendar,
        R.drawable.baseline_calendar_month_24,
        R.drawable.baseline_calendar_month_24 //here you can add not_field icon if you want. when its not selected
    ),
    NavigationItem(
        screen = Screen.Statistic,
        R.drawable.outline_bar_chart_24,
        R.drawable.outline_bar_chart_24
    ),
    NavigationItem(
        screen = Screen.Symptoms,
        R.drawable.baseline_bloodtype_24,
        R.drawable.baseline_bloodtype_24
    ),
    NavigationItem(
        screen = Screen.Settings,
        R.drawable.settings_24px,
        R.drawable.settings_24px
    ),
)