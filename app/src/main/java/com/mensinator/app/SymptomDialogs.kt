package com.mensinator.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mensinator.app.ui.theme.MensinatorTheme
import java.time.LocalDate


@Composable
fun SymptomsDialog(
    date: LocalDate,
    symptoms: List<Symptom>,
    dbHelper: IPeriodDatabaseHelper,
    onSave: (List<Symptom>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSymptoms by remember { mutableStateOf(emptySet<Symptom>()) }

    LaunchedEffect(date) {
        val symptomIdsForDate = dbHelper.getSymptomsFromDate(date).toSet()
        selectedSymptoms = symptoms.filter { it.id in symptomIdsForDate }.toSet()
    }

    AlertDialog(
        onDismissRequest = { onCancel() },
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
        modifier = modifier,
        dismissButton = {
            Button(
                onClick = {
                    onCancel()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.symptoms_dialog_title, date))
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
    )
}


@Composable
fun CreateNewSymptomDialog(
    newSymptom: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var symptomName by remember { mutableStateOf(newSymptom) }
    //val symptomKey = ResourceMapper.getStringResourceId(symptomName)

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = {
                    onSave(symptomName)
                },
            ) {
                Text(stringResource(id = R.string.save_button))
            }
        },
        modifier = modifier,
        dismissButton = {
            Button(
                onClick = onCancel,
            ) {
                Text(stringResource(id = R.string.cancel_button))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.create_new_symptom_dialog_title))
        },
        text = {
            TextField(
                //value = symptomKey?.let { stringResource(id = it) } ?: "Not Found",
                value = symptomName,
                onValueChange = { symptomName = it },
                label = { Text(stringResource(R.string.symptom_name_label)) },
                singleLine = true,
            )
        },
    )
}

@Composable
fun RenameSymptomDialog(
    symptomDisplayName: String,
    onRename: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var newName by remember { mutableStateOf(symptomDisplayName) }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = {
                    onRename(newName)
                },
            ) {
                Text(text = stringResource(id = R.string.save_button))
            }
        },
        modifier = modifier,
        dismissButton = {
            Button(
                onClick = onCancel,
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.rename_symptom))
        },
        text = {
            Column {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.symptom_name_label)) },
                    singleLine = true,
                )
            }
        },
    )
}

@Composable
fun DeleteSymptomDialog(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = onSave,
            ) {
                Text(text = stringResource(id = R.string.delete_button))
            }
        },
        modifier = modifier,
        dismissButton = {
            Button(
                onClick = onCancel,
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.delete_symptom))
        },
        text = {
            Text(text = stringResource(id = R.string.delete_question))
        },
    )
}

@Preview
@Composable
private fun CreateNewSymptomDialogPreview() {
    MensinatorTheme {
        CreateNewSymptomDialog(
            newSymptom = "preview",
            onSave = {},
            onCancel = {}
        )
    }
}

@Preview
@Composable
private fun RenameSymptomDialogPreview() {
    MensinatorTheme {
        RenameSymptomDialog(
            symptomDisplayName = "preview",
            onRename = {},
            onCancel = {}
        )
    }
}

@Preview
@Composable
private fun DeleteSymptomDialogPreview() {
    MensinatorTheme {
        DeleteSymptomDialog(
            onSave = {},
            onCancel = {}
        )
    }
}