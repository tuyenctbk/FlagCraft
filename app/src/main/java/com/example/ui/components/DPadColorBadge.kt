package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DPadDownBlue
import com.example.ui.theme.DPadLeftYellow
import com.example.ui.theme.DPadRightGreen
import com.example.ui.theme.DPadUpRed

enum class DPadDirection(
    val title: String,
    val color: Color,
    val icon: ImageVector,
    val testTag: String
) {
    UP("UP = RED", DPadUpRed, Icons.Default.ArrowUpward, "dpad_up_button"),
    LEFT("LEFT = YELLOW", DPadLeftYellow, Icons.AutoMirrored.Filled.ArrowBack, "dpad_left_button"),
    RIGHT("RIGHT = GREEN", DPadRightGreen, Icons.AutoMirrored.Filled.ArrowForward, "dpad_right_button"),
    DOWN("DOWN = BLUE", DPadDownBlue, Icons.Default.ArrowDownward, "dpad_down_button")
}

@Composable
fun DPadAnswerButton(
    direction: DPadDirection,
    optionText: String,
    flagPngUrl: String = "",
    isSelected: Boolean,
    isCorrect: Boolean?,
    isRevealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1.0f,
        animationSpec = spring(),
        label = "button_focus_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isRevealed && isCorrect == true -> Color(0xFF10B981) // Green success
            isRevealed && isSelected && isCorrect == false -> Color(0xFFEF4444) // Red fail
            isSelected -> direction.color
            isFocused -> direction.color.copy(alpha = 0.25f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "button_bg_color"
    )

    val borderModifier = if (isFocused) {
        Modifier.border(3.dp, Color.White, RoundedCornerShape(16.dp))
    } else if (isSelected) {
        Modifier.border(2.dp, direction.color, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(direction.testTag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(borderModifier)
            .clip(RoundedCornerShape(16.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(enabled = !isRevealed, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 10.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color Direction Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(direction.color)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = direction.icon,
                        contentDescription = direction.title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (flagPngUrl.isNotEmpty()) {
                    FlagImage(
                        flagUrl = flagPngUrl,
                        contentDescription = "Option Flag",
                        modifier = Modifier.size(width = 48.dp, height = 32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = optionText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = if (isSelected || isRevealed) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            if (isRevealed) {
                if (isCorrect == true) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Correct",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                } else if (isSelected && isCorrect == false) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Incorrect",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
