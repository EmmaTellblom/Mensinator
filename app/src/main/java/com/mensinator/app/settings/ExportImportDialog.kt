package com.mensinator.app.settings

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mensinator.app.R
import com.mensinator.app.data.ImportSource
import com.mensinator.app.ui.theme.MensinatorTheme
import java.io.File
import java.io.FileOutputStream

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
    onImportClick: (String, ImportSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    //val impSuccess = stringResource(id = R.string.import_success_toast)
    val impFailure = stringResource(id = R.string.import_failure_toast)

    var selectedOption by remember { mutableStateOf(ImportSource.MENSINATOR) }
    val options = ImportSource.entries.toTypedArray()

    // File import launcher
    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(defaultImportFilePath)
            val outputStream = FileOutputStream(file)
            try {
                inputStream?.copyTo(outputStream)
                onImportClick(file.absolutePath, selectedOption)
                //Toast.makeText(context, impSuccess, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, impFailure, Toast.LENGTH_SHORT).show()
                Log.d("ImportDialog", "Failed to import file: ${e.message}, ${e.stackTraceToString()}")
            } finally {
                inputStream?.close()
                outputStream.close()
            }
            onDismissRequest()
        }

    // Dialog content
    AlertDialog(
        text = {
            Column {
                DropdownMenu(
                    selectedOption = selectedOption,
                    onOptionSelected = { option ->
                        selectedOption = option
                    },
                    options = options,
                    label = stringResource(R.string.select_source),
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
                Text(stringResource(R.string.import_dialog_message))
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = { importLauncher.launch("application/json") }) {
                Text(stringResource(id = R.string.select_file_button))
            }
        },
        modifier = modifier,
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.cancel_button))
            }
        },
        title = { Text(stringResource(id = R.string.import_data)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(
    selectedOption: ImportSource,
    onOptionSelected: (ImportSource) -> Unit,
    options: Array<ImportSource>,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val roundedCornerShape = MaterialTheme.shapes.medium

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                readOnly = true,
                value = selectedOption.displayName,
                onValueChange = { },
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = roundedCornerShape,
                modifier = Modifier.menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
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
            onImportClick = { _, _ -> }
        )
    }
}