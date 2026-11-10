package com.common.shake.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class SensitivityTier(val label: String, val description: String) {
    HIGH("High", "Triggers with light wrist flick"),
    STANDARD("Standard", "Deliberate hand shake"),
    LOW("Low", "Vigorous shake (prevents false pocket triggers)")
}

fun classifyGForce(g: Float): SensitivityTier = when {
    g <= 2.0f -> SensitivityTier.HIGH
    g <= 3.0f -> SensitivityTier.STANDARD
    else -> SensitivityTier.LOW
}

@Composable
fun ShakeSensitivitySettingCard(
    isEnabled: Boolean,
    thresholdGForce: Float,
    onEnabledChanged: (Boolean) -> Unit,
    onThresholdChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val tier = remember(thresholdGForce) { classifyGForce(thresholdGForce) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Master Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.shake_setting_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.shake_setting_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = isEnabled, onCheckedChange = onEnabledChanged)
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                // Value readout & classification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.shake_sensitivity_label, tier.label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${String.format("%.1f", thresholdGForce)} g",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = tier.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Continuous Slider (1.5g to 4.5g in 0.1g steps)
                Slider(
                    value = thresholdGForce,
                    onValueChange = { onThresholdChanged((it * 10f).roundToInt() / 10f) },
                    valueRange = 1.5f..4.5f,
                    steps = 29,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Gentle (1.8g)" to 1.8f, "Normal (2.5g)" to 2.5f, "Firm (3.5g)" to 3.5f).forEach { (label, value) ->
                        OutlinedButton(
                            onClick = { onThresholdChanged(value) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
