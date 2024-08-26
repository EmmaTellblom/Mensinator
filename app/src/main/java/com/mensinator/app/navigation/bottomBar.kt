package com.mensinator.app.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mensinator.app.CalendarScreen
import com.mensinator.app.PeriodDatabaseHelper
import com.mensinator.app.R

enum class Screens {
    Home,
    Symptoms,
    Statistic,
    Settings
}

@Composable
fun BottomBar(
    navController: NavHostController = rememberNavController(),
    onScreenProtectionChanged: (Boolean) -> Unit?,
) {
    val context = LocalContext.current
    // For accessing database functions
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    // If protectScreen is 1, it should protect the screen
    // If protectScreen is 0, should not protect screen(allows prints and screen visibility in recent apps)
    val protectScreen = dbHelper.getSettingByKey("screen_protection")?.value?.toIntOrNull() ?: 1
    Log.d("screenProtectionUI", "protect screen value $protectScreen")
    onScreenProtectionChanged(protectScreen != 0)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screens.valueOf(
        backStackEntry?.destination?.route ?: Screens.Home.name
    )
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(currentScreen) { //for navigating back with back phone arrow
        val newIndex = when (currentScreen) {//this is not the best practise but works the same
            Screens.Home -> 0
            Screens.Statistic -> 1
            Screens.Symptoms -> 2
            Screens.Settings -> 3
            else -> 0 // Default value
        }

        if (selectedItemIndex != newIndex) {
            selectedItemIndex = newIndex
        }
    }

    Scaffold(
        bottomBar = {
            val barItems = listOf(
                BarItem(
                    R.string.home_page,
                    R.drawable.home_field,
                    R.drawable.home_field //here you can add not_field icon if you want. when its not selected
                ),
                BarItem(
                    R.string.statisic_page,
                    R.drawable.outline_bar_chart_24,
                    R.drawable.outline_bar_chart_24
                ),
                BarItem(
                    R.string.symptoms_page,
                    R.drawable.baseline_bloodtype_24,
                    R.drawable.baseline_bloodtype_24
                ),
                BarItem(
                    R.string.settings_page,
                    R.drawable.settings_24px,
                    R.drawable.settings_24px
                ),
            )
            NavigationBar {
                barItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        onClick = {
                            if (selectedItemIndex != index) {
                                when (index) {
                                    0 -> navController.navigate(Screens.Home.name) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }

                                    1 -> navController.navigate(Screens.Statistic.name) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }

                                    2 -> navController.navigate(Screens.Symptoms.name) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }

                                    3 -> navController.navigate(Screens.Settings.name) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                selectedItemIndex = index
                            }
                        },
                        selected = selectedItemIndex == index,
                        label = { Text(text = stringResource(item.title)) },
                        icon = {
                            Icon(
                                imageVector = if (index == selectedItemIndex) ImageVector.vectorResource(
                                    id = item.imageSelected
                                ) else ImageVector.vectorResource(id = item.imageUnSelected),
                                contentDescription = stringResource(id = item.title)
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screens.Home.name,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(route = Screens.Home.name) {//create a new file for every page and pass it inside the composable
                CalendarScreen()
            }
            composable(route = Screens.Statistic.name) {
                // here you add the page that you want to open(Statistic)
            }
            composable(route = Screens.Symptoms.name) {
                // here you add the page that you want to open(Symptoms)
            }
            composable(route = Screens.Settings.name) {
                // here you add the page that you want to open(Settings)
            }
        }
    }
}