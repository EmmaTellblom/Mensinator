package com.mensinator.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.mensinator.app.ui.theme.MensinatorTheme

@Composable
fun MensinatorTopBar(
    @StringRes titleStringId: Int,
    onTitleClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            //.windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        val modifier = onTitleClick?.let {
            Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { it() }
        } ?: Modifier
        Text(
            text = stringResource(titleStringId),
            modifier = modifier,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private class ScreenTitleProvider : PreviewParameterProvider<Int> {
    override val values: Sequence<Int>
        get() = Screen.entries.map { it.titleRes }.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun MensinatorTopBarPreview(
    @PreviewParameter(ScreenTitleProvider::class) stringId: Int,
) {
    MensinatorTheme {
        MensinatorTopBar(stringId)
    }
}