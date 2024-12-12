package com.mensinator.app

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource

@Composable
fun NotificationDialog(
    title: String,
    messageText: String,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit
) {
    var newMessageText by remember { mutableStateOf(messageText) }

    AlertDialog(
        title = { Text(title) },
        text = {
            TextField(
                value = newMessageText,
                onValueChange = { newMessageText = it },
                singleLine = false
            )
        },
        confirmButton = {
            Button(onClick = {
                onSave(newMessageText)
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.save_button))
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            Button(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        }
    )
}

