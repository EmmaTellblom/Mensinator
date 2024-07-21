package com.mensinator.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun NestedFAB(
    onStatisticsClick: () -> Unit,
    onFAQClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExportImportClick: () -> Unit,
    onManageSymptomsClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showFAQDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExportImportDialog by remember { mutableStateOf(false) }

    // Provide the context to the composable functions
    val context = LocalContext.current
    val exportImport = remember { ExportImport() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp) // Padding to ensure it's not cut off
    ) {
        // Main FAB
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd) // Align to bottom-right corner
                .navigationBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "More Actions"
            )
        }

        // Nested FABs
        if (expanded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .offset(y = (-80).dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            showFAQDialog = true
                            expanded = false  // Close the FAB menu when an option is selected
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "FAQ"
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            onStatisticsClick()  // Trigger the Statistics dialog callback
                            expanded = false  // Close the FAB menu when an option is selected
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_bar_chart_24),
                            contentDescription = "Statistics"
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            onSettingsClick()
                            expanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            onManageSymptomsClick()
                            // Handle the Symptoms click
                            expanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_bloodtype_24),
                            contentDescription = "Symptoms"
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            showExportImportDialog = true
                            expanded = false  // Close the FAB menu when an option is selected
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_save_24),
                            contentDescription = "Export Import"
                        )
                    }
                }
            }
        }

        // Show the FAQ Dialog
        if (showFAQDialog) {
            FAQDialog(
                onDismissRequest = {
                    showFAQDialog = false  // Close the FAQ dialog
                }
            )
        }

        // Show SettingsDialog
        if (showSettingsDialog) {
            SettingsDialog(
                onDismissRequest = {
                    showSettingsDialog = false  // Close the Settings dialog
                }
            )
        }

        // Show ExportImportDialog
        if (showExportImportDialog) {
            ExportImportDialog(
                onDismissRequest = {
                    showExportImportDialog = false  // Close the ExportImport dialog
                },
                onExportClick = { filePath ->
                    exportImport.exportDatabase(context, filePath)
                },
                onImportClick = { filePath ->
                    exportImport.importDatabase(context, filePath)
                }
            )
        }
    }
}
