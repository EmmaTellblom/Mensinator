package com.mensinator.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FAQDialog(
    onDismissRequest: () -> Unit // Callback to handle the close action
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxHeight = screenHeight * 0.60f

    AlertDialog(
        onDismissRequest = { onDismissRequest() },  // Call the dismiss callback when dialog is dismissed
        title = {
            Text(text = "Frequently Asked Questions")
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())  // Add vertical scrolling capability
                    .padding(16.dp)  // Padding around the text content
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                // User Manual Header
                Text(
                    text = "User Manual",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp)) // Space between sections

                // How to Use
                Text(
                    text = "How to Use:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "• Select Dates: Tap on a date to select or deselect it.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Add or Remove Dates for Period: Click the 'Add or Remove Dates' button after selecting dates.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Symptoms: Click the 'Symptoms' button to view or add symptoms for the selected date.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Statistics: Click the 'Burger menu' and select statistics.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Statistics: Click the 'Burger menu' and select statistics.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp)) // Space between sections

                Text(
                    text = "Features Coming Soon:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "• Reminder for upcoming period. (The button is added but not functional now in the settings)\n" +
                            "• Ability to inactivate symptoms (Button added but not functional now in the menu)\n" +
                            "• New user interface (when Im in the mood).\n" +
                            "• Other small UI improvements",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp)) // Space between sections

                // Our Story Header
                Text(
                    text = "Our Story",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "We are two women who were tired of selling our data to big corporations. Our app stores all your data locally on your device and we have **no access to it whatsoever**.\nWe value your privacy and do not save or share any personal information.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp)) // Space between sections

                // Disclaimer Header
                Text(
                    text = "Disclaimer:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "• This is a hobby-project and not intended for medical use. We are simply doing this for our own needs." +
                            "But we do welcome ideas and requests, send us an email if you have any questions\n" +
                            "mensinator.app@gmail.com",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismissRequest() }  // Call the dismiss callback when the button is clicked
            ) {
                Text("Close")
            }
        },
    )
}