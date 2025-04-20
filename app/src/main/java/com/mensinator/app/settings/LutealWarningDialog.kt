package com.mensinator.app.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mensinator.app.R
import com.mensinator.app.ui.theme.MensinatorTheme


@Composable
fun LutealWarningDialog(
    onDismissRequest: () -> Unit, // Callback to handle the close action
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,  // Call the dismiss callback when dialog is dismissed
        confirmButton = {
            Button(
                onClick = onDismissRequest  // Call the dismiss callback when the button is clicked
            ) {
                Text(stringResource(id = R.string.close_button))
            }
        },
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(text = stringResource(id = R.string.warning))
        },
        text = {
            Text(text = stringResource(id = R.string.luteal_calculation_message))
        },
    )
}

@Preview
@Composable
private fun LutealDialogWarningPreview() {
    MensinatorTheme {
        LutealWarningDialog(onDismissRequest = {})
    }
}