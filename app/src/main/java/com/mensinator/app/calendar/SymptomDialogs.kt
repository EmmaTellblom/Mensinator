package com.mensinator.app.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mensinator.app.R
import com.mensinator.app.data.Symptom
import com.mensinator.app.ui.ResourceMapper
import com.mensinator.app.ui.theme.MensinatorTheme
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
@Composable
fun EditSymptomsForDaysDialog(
    symptoms: PersistentSet<Symptom>,
    currentlyActiveSymptomIds: PersistentSet<Int>,
    onSave: (PersistentSet<Symptom>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSymptoms by remember {
        mutableStateOf(
            symptoms.filter { it.id in currentlyActiveSymptomIds }.toPersistentSet()
        )
    }

    AlertDialog(
        onDismissRequest = { onCancel() },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedSymptoms)
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
            Text(text = stringResource(id = R.string.symptoms_dialog_title))
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
                                val newSet = if (selectedSymptoms.contains(symptom)) {
                                    selectedSymptoms - symptom
                                } else {
                                    selectedSymptoms + symptom
                                }
                                selectedSymptoms = newSet.toPersistentSet()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedSymptoms.contains(symptom),
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = symptomDisplayName)
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun EditSymptomsForDaysDialog_OneDayPreview() {
    val symptoms = persistentSetOf(
        Symptom(1, "Heavy Flow", 0, ""),
        Symptom(2, "Light Flow", 1, ""),
        Symptom(3, "Medium Flow", 2, ""),
    )
    MensinatorTheme {
        EditSymptomsForDaysDialog(
            symptoms = symptoms,
            currentlyActiveSymptomIds = persistentSetOf(3),
            onSave = {},
            onCancel = { },
        )
    }
}

// TODO: Fix within https://github.com/EmmaTellblom/Mensinator/issues/203
@Preview
@Composable
private fun EditSymptomsForDaysDialog_MultipleDaysPreview() {
    val symptoms = persistentSetOf(
        Symptom(1, "Heavy Flow", 0, ""),
        Symptom(2, "Light Flow", 1, ""),
        Symptom(3, "Medium Flow", 2, ""),
    )
    MensinatorTheme {
        EditSymptomsForDaysDialog(
            symptoms = symptoms,
            currentlyActiveSymptomIds = persistentSetOf(3),
            onSave = {},
            onCancel = { },
        )
    }
}