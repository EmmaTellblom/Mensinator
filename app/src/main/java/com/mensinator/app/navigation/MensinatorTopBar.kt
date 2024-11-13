package com.mensinator.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.mensinator.app.ui.theme.MensinatorTheme

@Composable
fun MensinatorTopBar(screen: Screen) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(screen.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private class ScreenProvider : PreviewParameterProvider<Screen> {
    override val values: Sequence<Screen>
        get() = Screen.entries.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun MensinatorTopBarPreview(
    @PreviewParameter(ScreenProvider::class) screen: Screen,
) {
    MensinatorTheme {
        MensinatorTopBar(screen)
    }
}