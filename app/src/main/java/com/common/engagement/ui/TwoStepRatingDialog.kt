package com.common.engagement.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun TwoStepRatingDialog(
    appName: String,
    onLaunchStoreReview: () -> Unit,
    onRedirectInternalFeedback: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    if (step == 1) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = androidx.compose.ui.res.stringResource(com.example.R.string.rating_dialog_title, appName)) },
            text = { Text(text = androidx.compose.ui.res.stringResource(com.example.R.string.rating_dialog_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    step = 2
                    onLaunchStoreReview()
                }) {
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.rating_positive))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onRedirectInternalFeedback()
                    onDismiss()
                }) {
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.rating_negative))
                }
            }
        )
    }
}
