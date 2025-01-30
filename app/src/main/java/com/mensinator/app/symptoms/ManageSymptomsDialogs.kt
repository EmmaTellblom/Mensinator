package com.mensinator.app.symptoms

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mensinator.app.R
import com.mensinator.app.ui.theme.MensinatorTheme

@Composable
fun CreateNewSymptomDialog(
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var symptomName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = {
                    onSave(symptomName)
                },
                enabled = symptomName.isNotBlank()
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