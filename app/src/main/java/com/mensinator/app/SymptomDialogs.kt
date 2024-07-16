package com.mensinator.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun SymptomsDialog(
    date: LocalDate,
    symptoms: List<Symptom>,
    dbHelper: PeriodDatabaseHelper,
    onSave: (List<Symptom>) -> Unit,
    onCancel: () -> Unit,
    onCreateNewSymptom: () -> Unit  // Added callback for the new button
) {
    var selectedSymptoms by remember { mutableStateOf(emptySet<Symptom>()) }

    // LaunchedEffect to fetch symptom IDs and initialize selectedSymptoms
    LaunchedEffect(date) {
        val symptomIdsForDate = dbHelper.getSymptomsFromDate(date).toSet()
        selectedSymptoms = symptoms.filter { it.id in symptomIdsForDate }.toSet()
    }

    AlertDialog(
        onDismissRequest = { onCancel() },
        title = {
            Text(text = "Symptoms for $date")  // Display the date in the dialog title
        },
        text = {
            Column {
                symptoms.forEach { symptom ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                selectedSymptoms = if (selectedSymptoms.contains(symptom)) {
                                    selectedSymptoms - symptom
                                } else {
                                    selectedSymptoms + symptom
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedSymptoms.contains(symptom),
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = symptom.name, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onCreateNewSymptom()  // Call the new button click handler
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("Create New Symptom")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedSymptoms.toList())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save symptoms")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onCancel()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateNewSymptomDialog(
    newSymptom: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var symptomName by remember { mutableStateOf(newSymptom) }

    AlertDialog(
        onDismissRequest = { onCancel() },
        title = {
            Text(text = "Create New Symptom")
        },
        text = {
            TextField(
                value = symptomName,
                onValueChange = { symptomName = it },
                label = { Text("Symptom Name") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(symptomName)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onCancel()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}
