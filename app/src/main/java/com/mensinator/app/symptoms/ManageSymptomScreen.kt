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
import com.mensinator.app.symptoms.ManageSymptomsViewModel.UiAction
import com.mensinator.app.ui.ResourceMapper
import com.mensinator.app.ui.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.ui.theme.MensinatorTheme
import com.mensinator.app.ui.theme.UiConstants
import com.mensinator.app.ui.theme.isDarkMode
import org.koin.androidx.compose.koinViewModel

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
        setFabOnClick { viewModel.onAction(UiAction.ShowCreationDialog) }
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
                onAction = { viewModel.onAction(it) },
                symptom = symptom,
                showDeletionIcon = symptoms.size > 1
            )
        }
    }

    if (state.value.showCreateSymptomDialog) {
        CreateNewSymptomDialog(
            onSave = { newSymptomName ->
                viewModel.onAction(UiAction.CreateSymptom(newSymptomName))
                viewModel.onAction(UiAction.HideCreationDialog)
            },
            onCancel = {
                viewModel.onAction(UiAction.HideCreationDialog)
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
                val updatedSymptom = symptomToRename.copy(name = newName)
                viewModel.onAction(UiAction.RenameSymptom(updatedSymptom))
                viewModel.onAction(UiAction.HideRenamingDialog)
            },
            onCancel = {
                viewModel.onAction(UiAction.HideRenamingDialog)
            }
        )
    }

    val symptomToDelete = state.value.symptomToDelete
    if (symptomToDelete != null) {
        DeleteSymptomDialog(
            onSave = {
                viewModel.onAction(UiAction.DeleteSymptom(symptomToDelete))
                viewModel.onAction(UiAction.HideDeletionDialog)
            },
            onCancel = {
                viewModel.onAction(UiAction.HideDeletionDialog)
            },
        )
    }
}

@Composable
private fun SymptomItem(
    onAction: (uiAction: UiAction) -> Unit,
    symptom: Symptom,
    showDeletionIcon: Boolean,
    modifier: Modifier = Modifier,
) {
    val selectedColor = ColorSource.getColorMap(isDarkMode())[symptom.color] ?: Color.Gray
    val symptomDisplayName = ResourceMapper.getStringResourceOrCustom(symptom.name)

    Card(
        onClick = {
            onAction(UiAction.ShowRenamingDialog(symptom))
        },
        modifier = modifier.fillMaxWidth(),
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
                    onClick = { onAction(UiAction.ShowDeletionDialog(symptom)) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.close)
                    )
                }
            }
            Text(
                text = symptomDisplayName,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f) // Let the text expand to fill available space
                    .padding(4.dp)
            )

            ColorPicker(selectedColor, symptom, onAction)

            Spacer(modifier = Modifier.weight(0.05f))

            Switch(
                checked = symptom.isActive,
                onCheckedChange = { checked ->
                    val updatedSymptom = symptom.copy(active = if (checked) 1 else 0)
                    onAction(UiAction.UpdateSymptom(updatedSymptom))
                },
            )
            Spacer(modifier = Modifier.weight(0.05f))
        }
    }
}

// Color Picker Dropdown Menu
@Composable
private fun ColorPicker(
    selectedColor: Color,
    symptom: Symptom,
    onAction: (uiAction: UiAction) -> Unit,
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
                                    onAction(UiAction.UpdateSymptom(updatedSymptom))
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

@Preview(showBackground = true)
@Composable
private fun SymptomItemPreview() {
    MensinatorTheme {
        SymptomItem(
            onAction = {},
            symptom = Symptom(1, "Medium flow", 1, "Red"),
            showDeletionIcon = true,
            modifier = Modifier.padding(8.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun SymptomItemLongTextPreview() {
    MensinatorTheme {
        SymptomItem(
            onAction = {},
            symptom = Symptom(2, "Very long text that could span multiple lines ".repeat(2), 0, "DarkBlue"),
            showDeletionIcon = false,
            modifier = Modifier.padding(8.dp)
        )
    }
}