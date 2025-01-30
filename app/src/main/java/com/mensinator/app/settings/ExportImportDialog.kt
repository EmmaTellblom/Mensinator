package com.mensinator.app.settings

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mensinator.app.R
import com.mensinator.app.ui.theme.MensinatorTheme
import java.io.File
import java.io.FileOutputStream

@Composable
fun ExportDialog(
    exportFilePath: String,
    onDismissRequest: () -> Unit,
    onExportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val expSuccess = stringResource(id = R.string.export_success_toast, exportFilePath)
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    onExportClick(exportFilePath) // Calls the exported function
                    Toast.makeText(context, expSuccess, Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                },
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
            Text(stringResource(id = R.string.export_path_label, exportFilePath))
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

    val importPath = remember { mutableStateOf("") }
    val impSuccess = stringResource(id = R.string.import_success_toast)
    val impFailure = stringResource(id = R.string.import_failure_toast)

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(defaultImportFilePath)
            val outputStream = FileOutputStream(file)
            try {
                inputStream?.copyTo(outputStream)
                importPath.value = file.absolutePath
                // Call the import function
                onImportClick(importPath.value)
                // Show success toast
                Toast.makeText(context, impSuccess, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Show error toast
                Toast.makeText(context, impFailure, Toast.LENGTH_SHORT).show()
                Log.d(
                    "ExportImportDialog",
                    "Failed to import file: ${e.message}, ${e.stackTraceToString()}"
                )
            } finally {
                // Clean up
                inputStream?.close()
                outputStream.close()
            }
            // Dismiss the dialog after importing
            onDismissRequest()
        }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    importLauncher.launch("application/json")
                },
            ) {
                Text(stringResource(id = R.string.select_file_button))
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
            Text(stringResource(id = R.string.import_data))
        },
        text = {
            Text(
                stringResource(R.string.select_file)
            )
        }
    )
}

@Preview
@Composable
private fun ExportDialogPreview() {
    MensinatorTheme {
        ExportDialog(
            exportFilePath = "/preview/path/example",
            onDismissRequest = {},
            onExportClick = {}
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