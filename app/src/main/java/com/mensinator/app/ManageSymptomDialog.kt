package com.mensinator.app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mensinator.app.data.DataSource
import com.mensinator.app.ui.theme.isDarkMode


//Maps Database keys to res/strings.xml for multilanguage support
@Composable
fun ManageSymptom(
    showCreateSymptom: MutableState<Boolean>,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    var initialSymptoms = remember { dbHelper.getAllSymptoms() }
    var savedSymptoms by remember { mutableStateOf(initialSymptoms) }

    // State to manage the rename dialog visibility
    var showRenameDialog by remember { mutableStateOf(false) }
    var symptomToRename by remember { mutableStateOf<Symptom?>(null) }

    // State to manage the dialog visibility
    var showDeleteDialog by remember { mutableStateOf(false) }
    var symptomToDelete by remember { mutableStateOf<Symptom?>(null) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val menuHeight = screenHeight * 0.8f // 80% of the screen height

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),  // Make the column scrollable
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.symptoms_button),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f)) //cover available space
        }
        Spacer(modifier = Modifier.padding(3.dp))
        savedSymptoms.forEach { symptom ->
            var expanded by remember { mutableStateOf(false) }
            var selectedColorName by remember { mutableStateOf(symptom.color) }
            //val resKey = ResourceMapper.getStringResourceId(symptom.name)
            val selectedColor = DataSource(isDarkMode()).colorMap[selectedColorName] ?: Color.Gray


            val symptomKey = ResourceMapper.getStringResourceId(symptom.name)
            val symptomDisplayName = symptomKey?.let { stringResource(id = it) } ?: symptom.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp)
                    .clickable {
                        symptomToRename = symptom
                        showRenameDialog = true
                    },
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(),
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
                                if (dbHelper.getAllActiveSymptoms().contains(symptom)) {
                                    Log.d("test2", dbHelper.getAllActiveSymptoms().toString())
                                    showDeleteDialog = true
                                    symptomToDelete = symptom

                                } else {
                                    symptom.let { symptom ->
                                        savedSymptoms = savedSymptoms.filter { it.id != symptom.id }
                                        dbHelper.deleteSymptom(symptom.id)
                                        onSave()
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
                            modifier = Modifier
                                .width(50.dp)
                                .height(menuHeight)
                                .clip(RoundedCornerShape(100.dp))
                        ) {
                            DataSource(isDarkMode()).colorMap.forEach { (colorName, colorValue) ->
                                //val keyColor = ResourceMapper.getStringResourceId(colorName)
                                DropdownMenuItem(
                                    onClick = {
                                        selectedColorName = colorName
                                        expanded = false
                                        val updatedSymptom = symptom.copy(color = colorName)
                                        savedSymptoms = savedSymptoms.map {
                                            if (it.id == symptom.id) updatedSymptom else it
                                        }
                                        // Save settings to the database
                                        savedSymptoms.forEach { symptom ->
                                            dbHelper.updateSymptom(
                                                symptom.id,
                                                symptom.active,
                                                symptom.color
                                            )
                                        }
                                        onSave()
                                    },
                                    text = {
                                        Box(
                                            modifier = Modifier
                                                .size(25.dp)
                                                .clip(RoundedCornerShape(26.dp))
                                                .background(colorValue),  // Use the color from the map
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(0.5f))

                    Switch(
                        checked = symptom.active == 1,
                        onCheckedChange = { checked ->
                            val updatedSymptom = symptom.copy(active = if (checked) 1 else 0)
                            savedSymptoms = savedSymptoms.map {
                                if (it.id == symptom.id) updatedSymptom else it
                            }
                            // Save settings to the database
                            savedSymptoms.forEach { symptom ->
                                dbHelper.updateSymptom(symptom.id, symptom.active, symptom.color)
                            }
                            onSave()
                        },
                        colors = SwitchDefaults.colors(
                        )
                    )
                    Spacer(modifier = Modifier.weight(0.1f))
                }
            }
        }
    }
    if (showCreateSymptom.value) {
        CreateNewSymptomDialog(
            newSymptom = "",  // Pass an empty string for new symptoms
            onSave = { newSymptomName ->
                dbHelper.createNewSymptom(newSymptomName)
                initialSymptoms =
                    dbHelper.getAllSymptoms() //reset the data to make the new symptom appear
                savedSymptoms = initialSymptoms
                showCreateSymptom.value = false  // Close the new symptom dialog
            },
            onCancel = {
                showCreateSymptom.value = false  // Close the new symptom dialog
            },
        )
    }

    if (showRenameDialog && symptomToRename != null) {
        val symptomKey = ResourceMapper.getStringResourceId(symptomToRename!!.name)
        val symptomDisplayName =
            symptomKey?.let { stringResource(id = it) } ?: symptomToRename!!.name

        RenameSymptomDialog(
            symptomDisplayName = symptomDisplayName,
            onRename = { newName ->
                dbHelper.renameSymptom(symptomToRename!!.id, newName)
                initialSymptoms = dbHelper.getAllSymptoms()
                savedSymptoms = initialSymptoms
                showRenameDialog = false
            },
            onCancel = {
                showRenameDialog = false
            }
        )
    }

// Show the delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(text = stringResource(id = R.string.delete_symptom))
            },
            text = {
                Text(text = stringResource(id = R.string.delete_question))
            },
            confirmButton = {
                Button(
                    onClick = {
                        symptomToDelete?.let { symptom ->
                            savedSymptoms = savedSymptoms.filter { it.id != symptom.id }
                            dbHelper.deleteSymptom(symptom.id)
                            onSave()
                        }

                        showDeleteDialog = false
                    },
                    modifier = Modifier.padding(end = 27.dp)
                ) {
                    Text(text = stringResource(id = R.string.delete_button))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                    },
                    modifier = Modifier.padding(end = 30.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancel_button))
                }
            }
        )
    }
}

@Composable
fun RenameSymptomDialog(
    symptomDisplayName: String,
    onRename: (String) -> Unit,
    onCancel: () -> Unit
) {
    var newName by remember { mutableStateOf(symptomDisplayName) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(id = R.string.rename_symptom))
        },
        text = {
            Column {
                Spacer(modifier = Modifier.size(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRename(newName)
                },
                modifier = Modifier.padding(end = 27.dp)
            ) {
                Text(text = stringResource(id = R.string.save_button))
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                modifier = Modifier.padding(end = 30.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        }
    )
}