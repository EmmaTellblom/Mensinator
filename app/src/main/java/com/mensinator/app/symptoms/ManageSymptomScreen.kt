package com.mensinator.app.symptoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mensinator.app.R
import com.mensinator.app.data.ColorSource
import com.mensinator.app.data.Symptom
import com.mensinator.app.data.isActive
import com.mensinator.app.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.settings.ResourceMapper
import com.mensinator.app.ui.theme.MensinatorTheme
import com.mensinator.app.ui.theme.UiConstants
import com.mensinator.app.ui.theme.isDarkMode
import org.koin.androidx.compose.koinViewModel

// TODO: Maybe delete savedSymptoms

private object SymptomScreenConstants {
    val colorCircleSize = 24.dp
}

@Composable
fun ManageSymptomScreen(
    modifier: Modifier = Modifier,
    viewModel: ManageSymptomsViewModel = koinViewModel(),
    setFabOnClick: (() -> Unit) -> Unit,
) {
    val state = viewModel.viewState.collectAsState()
    val symptoms = state.value.allSymptoms

    LaunchedEffect(Unit) {
        setFabOnClick { viewModel.showCreateSymptomDialog(true) }
        viewModel.refreshData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())  // Make the column scrollable
            .displayCutoutExcludingStatusBarsPadding()
            .padding(16.dp)
            .padding(bottom = UiConstants.floatingActionButtonSize * 1.25f), // To be able to overscroll the list, to not have the FloatingActionButton overlapping
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        symptoms.forEach { symptom ->
            SymptomItem(
                viewModel = viewModel,
                symptom = symptom,
                showDeletionIcon = symptoms.size > 1
            )
        }
    }

    if (state.value.showCreateSymptomDialog) {
        CreateNewSymptomDialog(
            onSave = { newSymptomName ->
                viewModel.createNewSymptom(newSymptomName)
                viewModel.showCreateSymptomDialog(false)
            },
            onCancel = {
                viewModel.showCreateSymptomDialog(false)
            },
        )
    }

    val symptomToRename = state.value.symptomToRename
    if (symptomToRename != null) {
        val symptomKey = ResourceMapper.getStringResourceId(symptomToRename.name)
        val symptomDisplayName = symptomKey?.let { stringResource(id = it) } ?: symptomToRename.name

        RenameSymptomDialog(
            symptomDisplayName = symptomDisplayName,
            onRename = { newName ->
                viewModel.renameSymptom(symptomToRename.id, newName)
                viewModel.setSymptomToRename(null)
            },
            onCancel = {
                viewModel.setSymptomToRename(null)
            }
        )
    }

    val symptomToDelete = state.value.symptomToDelete
    if (symptomToDelete != null) {
        DeleteSymptomDialog(
            onSave = {
                viewModel.deleteSymptom(symptomToDelete.id)
                viewModel.setSymptomToDelete(null)
            },
            onCancel = {
                viewModel.setSymptomToDelete(null)
            },
        )
    }
}

@Composable
private fun SymptomItem(
    viewModel: ManageSymptomsViewModel,
    symptom: Symptom,
    showDeletionIcon: Boolean
) {
    val selectedColor = ColorSource.getColorMap(isDarkMode())[symptom.color] ?: Color.Gray
    val symptomDisplayName = ResourceMapper.getStringResourceOrCustom(symptom.name)

    Card(
        onClick = {
            viewModel.setSymptomToRename(symptom)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            if (showDeletionIcon) {
                IconButton(
                    onClick = { viewModel.setSymptomToDelete(symptom) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.close)
                    )
                }
            }
            Spacer(modifier = Modifier.padding(start = 5.dp))
            Text(
                text = symptomDisplayName,
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(1f) // Let the text expand to fill available space
            )

            //Color Picker Dropdown Menu
            ColorPicker(selectedColor, symptom, viewModel)

            Spacer(modifier = Modifier.weight(0.05f))

            Switch(
                checked = symptom.isActive,
                onCheckedChange = { checked ->
                    val updatedSymptom = symptom.copy(active = if (checked) 1 else 0)
                    viewModel.updateSymptom(
                        updatedSymptom.id,
                        updatedSymptom.active,
                        updatedSymptom.color
                    )
                },
            )
            Spacer(modifier = Modifier.weight(0.05f))
        }
    }
}

@Composable
private fun ColorPicker(
    selectedColor: Color,
    symptom: Symptom,
    viewModel: ManageSymptomsViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // Color Dropdown wrapped in a Box for alignment
        Card(
            onClick = { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(SymptomScreenConstants.colorCircleSize)
                        .clip(CircleShape)
                        .background(selectedColor),
                )
                Icon(
                    painter = painterResource(id = R.drawable.keyboard_arrow_down_24px),
                    contentDescription = stringResource(id = R.string.selection_color),
                    modifier = Modifier.wrapContentSize()
                )
            }
        }

        DropdownMenu(
            offset = DpOffset(x = (-50).dp, y = (10).dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentSize()
        ) {
            // Retrieve the colorMap from DataSource
            val colorMap = ColorSource.getColorMap(isDarkMode())

            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ColorSource.colorsGroupedByHue.forEach { colorGroup ->
                    Row(
                        modifier = Modifier.wrapContentSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorGroup.forEach { colorName ->
                            val colorValue = colorMap[colorName] ?: return@Row
                            DropdownMenuItem(
                                modifier = Modifier
                                    .size(SymptomScreenConstants.colorCircleSize * 2)
                                    .clip(CircleShape),
                                onClick = {
                                    expanded = false
                                    val updatedSymptom = symptom.copy(color = colorName)
                                    viewModel.updateSymptom(
                                        updatedSymptom.id,
                                        updatedSymptom.active,
                                        updatedSymptom.color
                                    )
                                },
                                text = {
                                    Box(
                                        modifier = Modifier
                                            .size(SymptomScreenConstants.colorCircleSize)
                                            .clip(CircleShape)
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


// TODO: Broken
@Preview(showBackground = true)
@Composable
private fun SymptomItemPreview() {
    MensinatorTheme {
        SymptomItem(
            viewModel = koinViewModel(),
            symptom = Symptom(1, "Medium flow", 1, "red"),
            showDeletionIcon = true
        )
    }
}