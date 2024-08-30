package com.mensinator.app

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.res.stringResource



@Composable
fun ExportImportDialog(
    onDismissRequest: () -> Unit,
    onExportClick: (String) -> Unit,
    onImportClick: (String) -> Unit
) {
    val context = LocalContext.current
    val exportImport = remember { ExportImport() }

    val exportPath = remember { mutableStateOf(exportImport.getDocumentsExportFilePath()) }
    val importPath = remember { mutableStateOf("") }
    val impSuccess = stringResource(id = R.string.import_success_toast)
    val impFailure = stringResource(id = R.string.import_failure_toast)

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(exportImport.getDefaultImportFilePath(context))
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
                Log.d("ExportImportDialog", "Failed to import file: ${e.message}, ${e.stackTraceToString()}")
            } finally {
                // Clean up
                inputStream?.close()
                outputStream.close()
            }
            // Dismiss the dialog after importing
            onDismissRequest()
        }
    }

    val expSuccess = stringResource(id = R.string.export_success_toast, exportPath.value)
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = {
                onExportClick(exportPath.value) // Calls the exported function
                Toast.makeText(context, expSuccess, Toast.LENGTH_SHORT).show()
                onDismissRequest()
            }) {
                Text(stringResource(id = R.string.export_button))
            }
        },
        dismissButton = {
            Button(onClick = {
                importLauncher.launch("application/json")
            }) {
                Text(stringResource(id = R.string.import_button))
            }
        },
        title = {
            Text(stringResource(id = R.string.export_import_title))
        },
        text = {
            Column {
                Text(stringResource(id = R.string.export_path_label, exportPath.value))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(id = R.string.import_path_label, importPath.value))
            }
        }
    )
}