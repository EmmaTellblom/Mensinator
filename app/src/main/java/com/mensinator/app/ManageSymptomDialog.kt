package com.mensinator.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ManageSymptom(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { PeriodDatabaseHelper(context) }
    val initialSymptoms = remember { dbHelper.getAllSymptoms() }
    var savedSymptoms by remember { mutableStateOf(initialSymptoms) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Manage Symptoms", fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())  // Make the column scrollable
            ) {
                savedSymptoms.forEach { symptom ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = symptom.name, fontSize = 16.sp)
                        Switch(
                            checked = symptom.active == 1,
                            onCheckedChange = { checked ->
                                val updatedSymptom = symptom.copy(active = if (checked) 1 else 0)
                                savedSymptoms = savedSymptoms.map {
                                    if (it.id == symptom.id) updatedSymptom else it
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Save settings to the database
                savedSymptoms.forEach { symptom ->
                    dbHelper.updateSymptom(symptom.id, symptom.active)
                }
                onDismissRequest()  // Close the dialog
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Close")
            }
        },
        // To adjust the width of the AlertDialog based on screen size
        modifier = Modifier.width(LocalConfiguration.current.screenWidthDp.dp * 0.9f) // Adjust width to 90% of screen width
    )
}