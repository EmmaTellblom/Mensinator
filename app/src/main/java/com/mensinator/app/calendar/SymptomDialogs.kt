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
import java.time.LocalDate

@Composable
fun EditSymptomsForDaysDialog(
    date: LocalDate,
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
        Symptom(1, "Light", 0, ""),
        Symptom(2, "Medium", 1, ""),
    )
    MensinatorTheme {
        EditSymptomsForDaysDialog(
            date = LocalDate.now(),
            symptoms = symptoms,
            currentlyActiveSymptomIds = persistentSetOf(2),
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
        Symptom(1, "Light", 0, ""),
        Symptom(2, "Medium", 1, ""),
    )
    MensinatorTheme {
        EditSymptomsForDaysDialog(
            date = LocalDate.now(),
            symptoms = symptoms,
            currentlyActiveSymptomIds = persistentSetOf(2),
            onSave = {},
            onCancel = { },
        )
    }
}