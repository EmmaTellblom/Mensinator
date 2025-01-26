package com.mensinator.app.symptoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mensinator.app.R
import com.mensinator.app.data.ColorSource
import com.mensinator.app.settings.ResourceMapper
import com.mensinator.app.navigation.displayCutoutExcludingStatusBarsPadding
import com.mensinator.app.ui.theme.isDarkMode
import org.koin.androidx.compose.koinViewModel

// TODO: Improve Composable structure
// TODO: Use tokens for shapes
// TODO: Maybe delete savedSymptoms
// TODO: Define/use constant for 50.dp FAB size
// TODO:
// TODO:
// TODO:
// TODO:

//Maps Database keys to res/strings.xml for multilanguage support
@Composable
fun ManageSymptomScreen(
    modifier: Modifier = Modifier,
    viewModel: ManageSymptomsViewModel = koinViewModel(),
    setFabOnClick: (() -> Unit) -> Unit,
) {
    val state = viewModel.viewState.collectAsState()
    var savedSymptoms = state.value.allSymptoms

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
            .padding(bottom = 50.dp), // To be able to overscroll the list, to not have the FloatingActionButton overlapping
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        savedSymptoms.forEach { symptom ->
            var expanded by remember { mutableStateOf(false) }
            var selectedColorName by remember { mutableStateOf(symptom.color) }
            //val resKey = ResourceMapper.getStringResourceId(symptom.name)
            val selectedColor =
                ColorSource.getColorMap(isDarkMode())[selectedColorName] ?: Color.Gray

            val symptomDisplayName = ResourceMapper.getStringResourceOrCustom(symptom.name)
            Card(
                onClick = {
                    viewModel.setSymptomToRename(symptom)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp)
                ) {
                    if (savedSymptoms.size > 1) {
                        IconButton(
                            onClick = {
                                val activeSymptoms = state.value.activeSymptoms
                                if (activeSymptoms.contains(symptom)) {
                                    viewModel.setSymptomToDelete(symptom)
                                } else {
                                    symptom.let { symptom ->
                                        savedSymptoms = savedSymptoms.filter { it.id != symptom.id }
                                        viewModel.deleteSymptom(symptom.id)
                                    }
                                }
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
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
                    Box {
                        // Color Dropdown wrapped in a Box for alignment
                        Card(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .clickable { }
                                .clip(RoundedCornerShape(26.dp)),  // Make the entire row round
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
                                        .size(25.dp)
                                        .clip(RoundedCornerShape(26.dp))
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
                            modifier = Modifier
                                .wrapContentSize()
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
                                            val colorValue = colorMap[colorName]
                                            if (colorValue != null) {
                                                DropdownMenuItem(
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .clip(CircleShape),
                                                    onClick = {
                                                        selectedColorName = colorName
                                                        expanded = false
                                                        val updatedSymptom =
                                                            symptom.copy(color = colorName)
                                                        savedSymptoms = savedSymptoms.map {
                                                            if (it.id == symptom.id) updatedSymptom else it
                                                        }
                                                        // Save settings to the database
                                                        savedSymptoms.forEach { symptom ->
                                                            viewModel.updateSymptom(
                                                                symptom.id,
                                                                symptom.active,
                                                                symptom.color
                                                            )
                                                        }
                                                    },
                                                    text = {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(25.dp)
                                                                .clip(RoundedCornerShape(26.dp))
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

                    Spacer(modifier = Modifier.weight(0.05f))

                    Switch(
                        checked = symptom.active == 1,
                        onCheckedChange = { checked ->
                            val updatedSymptom =
                                symptom.copy(active = if (checked) 1 else 0)
                            savedSymptoms = savedSymptoms.map {
                                if (it.id == symptom.id) updatedSymptom else it
                            }
                            // Save settings to the database
                            savedSymptoms.forEach { symptom ->
                                viewModel.updateSymptom(
                                    symptom.id,
                                    symptom.active,
                                    symptom.color
                                )
                            }
                        },
                    )
                    Spacer(modifier = Modifier.weight(0.05f))
                }
            }
        }
    }

    if (state.value.showCreateSymptomDialog) {
        // TODO: remove newSymptom parameter?
        CreateNewSymptomDialog(
            newSymptom = "",  // Pass an empty string for new symptoms
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
        val symptomDisplayName =
            symptomKey?.let { stringResource(id = it) } ?: symptomToRename.name

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
