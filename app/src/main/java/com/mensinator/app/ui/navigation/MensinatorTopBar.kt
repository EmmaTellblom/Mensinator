package com.mensinator.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.mensinator.app.R
import com.mensinator.app.ui.theme.MensinatorTheme

@Composable
fun MensinatorTopBar(
    @StringRes titleStringId: Int,
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            //.windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            }
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