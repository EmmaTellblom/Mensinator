package com.mensinator.app

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mensinator.app.data.DataSource


//Maps Database keys to res/strings.xml for multilanguage support
@Composable
fun ManageSymptom(
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    val initialSymptoms = remember { dbHelper.getAllSymptoms() }
    var savedSymptoms by remember { mutableStateOf(initialSymptoms) }

    // State to manage the dialog visibility
    var showDeleteDialog by remember { mutableStateOf(false) }
    var symptomToDelete by remember { mutableStateOf<Symptom?>(null) }

    Column(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),  // Make the column scrollable
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp)
        ) {
            Text(
                text = "Flow",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f)) //cover available space
            Text(
                text = "Color",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.padding(3.dp))
        savedSymptoms.forEach { symptom ->
            var expanded by remember { mutableStateOf(false) }
            var selectedColorName by remember { mutableStateOf(symptom.color) }
            //val resKey = ResourceMapper.getStringResourceId(symptom.name)
            val colorIndex =
                DataSource().predefinedListOfColors.indexOfFirst { it.first == selectedColorName }
            val selectedColor =
                DataSource().predefinedListOfColors.getOrNull(colorIndex)?.second ?: Color.Gray

            val symptomKey = ResourceMapper.getStringResourceId(symptom.name)
            val symptomDisplayName = symptomKey?.let { stringResource(id = it) } ?: symptom.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFf5ebff) //change this color when you implement a theme
                )                                           // to something like : MaterialTheme.colorScheme.onBackground
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
                                    //TODO("make a data fun to delete one symptom")
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
                        }
                    )
                    Spacer(modifier = Modifier.weight(0.2f))
                    // Color Dropdown wrapped in a Box for alignment
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .padding(4.dp),
                            border = BorderStroke(
                                2.dp,
                                selectedColor
                            ) // Set the border color to the selected color
                        ) {
                            Text(
                                text = selectedColorName,
                                textAlign = TextAlign.Left
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


                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DataSource().predefinedListOfColors.forEach { (colorName) ->
                                val keyColor = ResourceMapper.getStringResourceId(colorName)

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
                                        Text(
                                            text = keyColor?.let { stringResource(id = it) }
                                                ?: "Not found",
                                            textAlign = TextAlign.Left
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
                        /*TODO("make a data fun to delete one symptom")
                        symptomToDelete?.let { symptom ->
                            savedSymptoms = savedSymptoms.filter { it.id != symptom.id }
                            dbHelper.deleteSymptom(symptom.id)
                            onSave()
                        }
                        */
                        showDeleteDialog = false
                    },
                    modifier = Modifier.padding(end = 15.dp)
                ) {
                    Text(text = stringResource(id = R.string.delete_button))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                    },
                    modifier = Modifier.padding(end = 50.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancel_button))
                }
            }
        )
    }
}