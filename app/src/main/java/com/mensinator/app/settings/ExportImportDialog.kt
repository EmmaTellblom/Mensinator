package com.mensinator.app.settings

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mensinator.app.R
import com.mensinator.app.ui.theme.MensinatorTheme
import java.io.File
import java.io.FileOutputStream
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@Composable
fun ExportDialog(
    defaultFileName: String,
    onDismissRequest: () -> Unit,
    onPathSelect: (exportUri: Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val jsonMimeType = "application/json"
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(jsonMimeType)) { uri ->
            if (uri == null) {
                onDismissRequest()
                return@rememberLauncherForActivityResult
            }

            onPathSelect(uri)
            onDismissRequest()
        }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = { filePickerLauncher.launch(defaultFileName) },
            ) {
                Text(stringResource(id = R.string.export_button))
            }
        },
        modifier = modifier,
        dismissButton = {
            Button(
                onClick = {
                    onDismissRequest()
                },
            ) {
                Text(stringResource(id = R.string.cancel_button))
            }
        },
        title = {
            Text(stringResource(id = R.string.export_data))
        },
        text = {
            Text(stringResource(id = R.string.export_dialog_message))
        }
    )
}

@Composable
fun ImportDialog(
    defaultImportFilePath: String,
    onDismissRequest: () -> Unit,
    onImportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val impSuccess = stringResource(id = R.string.import_success_toast)
    val impFailure = stringResource(id = R.string.import_failure_toast)

    var selectedOption by remember { mutableStateOf("Mensinator") }
    val options = listOf("Mensinator", "Clue")

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(defaultImportFilePath)
            val outputStream = FileOutputStream(file)
            try {
                inputStream?.copyTo(outputStream)
                // TODO:  Pass file source
                onImportClick(file.absolutePath)
                Toast.makeText(context, impSuccess, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, impFailure, Toast.LENGTH_SHORT).show()
                Log.d("ExportImportDialog", "Failed to import file: ${e.message}, ${e.stackTraceToString()}")
            } finally {
                inputStream?.close()
                outputStream.close()
            }

            onDismissRequest()
        }

    AlertDialog(
        text = {
            Column {
                DropdownMenu(
                    selectedOption = selectedOption,
                    onOptionSelected = { selectedOption = it },
                    options = options,
                    label = stringResource(R.string.select_source),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.import_dialog_message))
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = {
                importLauncher.launch("application/json")
            }) {
                Text(stringResource(id = R.string.select_file_button))
            }
        },
        modifier = modifier,
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.cancel_button))
            }
        },
        title = {
            Text(stringResource(id = R.string.import_data))
        }
    )
}

// The material3 dropdown is marked as experimental
// So change this when its fully implemented
@Composable
fun DropdownMenu(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    options: List<String>,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown arrow"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExportDialogPreview() {
    MensinatorTheme {
        ExportDialog(
            defaultFileName = "mensinator.json",
            onDismissRequest = {},
            onPathSelect = {}
        )
    }
}

@Preview
@Composable
private fun ImportDialogPreview() {
    MensinatorTheme {
        ImportDialog(
            defaultImportFilePath = "",
            onDismissRequest = {},
            onImportClick = {}
        )
    }
}