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
import androidx.compose.ui.res.stringResource





@Composable
fun SymptomsDialog(
    date: LocalDate,
    symptoms: List<Symptom>,
    dbHelper: PeriodDatabaseHelper,
    onSave: (List<Symptom>) -> Unit,
    onCancel: () -> Unit
) {
    var selectedSymptoms by remember { mutableStateOf(emptySet<Symptom>()) }

    LaunchedEffect(date) {
        val symptomIdsForDate = dbHelper.getSymptomsFromDate(date).toSet()
        selectedSymptoms = symptoms.filter { it.id in symptomIdsForDate }.toSet()
    }

    AlertDialog(
        onDismissRequest = { onCancel() },
        title = {
            Text(text = stringResource(id = R.string.symptoms_dialog_title, date))
        },
        text = {
            Column {
                symptoms.forEach { symptom ->
                    val symptomKey = ResourceMapper.getStringResourceId(symptom.name)
                    val symptomDisplayName = symptomKey?.let { stringResource(id = it) } ?: symptom.name
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
                        Text(text = symptomDisplayName, fontSize = 16.sp)
                    }
                }
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(
//                    onClick = {
//                        onCreateNewSymptom()
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(text = stringResource(id = R.string.create_new_symptom_button))
//                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedSymptoms.toList())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.save_symptoms_button))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onCancel()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
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
    //val symptomKey = ResourceMapper.getStringResourceId(symptomName)

    AlertDialog(
        onDismissRequest = { onCancel() },
        title = {
            Text(text = stringResource(id = R.string.create_new_symptom_dialog_title))
        },
        text = {
            TextField(
                //value = symptomKey?.let { stringResource(id = it) } ?: "Not Found",
                value = symptomName,
                onValueChange = { symptomName = it },
                label = { Text(stringResource(R.string.symptom_name_label)) }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(symptomName)
                },
                modifier = Modifier.padding(end = 27.dp)
            ) {
                Text(stringResource(id = R.string.save_button))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onCancel()
                },
                modifier = Modifier.padding(end = 30.dp)
            ) {
                Text(stringResource(id = R.string.cancel_button))
            }
        }
    )
}
