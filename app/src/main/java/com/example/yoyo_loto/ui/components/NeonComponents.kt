package com.example.yoyo_loto.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.yoyo_loto.ui.theme.DarkSurfaceAlt
import com.example.yoyo_loto.ui.theme.NeonBlue
import com.example.yoyo_loto.ui.theme.TextMuted

private val CardShape = RoundedCornerShape(18.dp)
private const val CardAlpha = 0.78f

@Composable
fun Modifier.neonGlow(
    color: Color,
    blur: Dp = 18.dp,
    shape: RoundedCornerShape = CardShape
): Modifier {
    val surface = MaterialTheme.colorScheme.surface.copy(alpha = CardAlpha)
    return this
        .shadow(
            elevation = blur,
            shape = shape,
            ambientColor = color.copy(alpha = 0.35f),
            spotColor = color.copy(alpha = 0.45f)
        )
        .background(surface, shape)
        .border(BorderStroke(1.dp, color.copy(alpha = 0.55f)), shape)
        .shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.25f),
            spotColor = Color.Black.copy(alpha = 0.35f)
        )
}

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonBlue,
    content: @Composable () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface.copy(alpha = CardAlpha)
    Surface(
        modifier = modifier.neonGlow(glowColor),
        color = surface,
        shape = CardShape,
        content = content
    )
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    color: Color = NeonBlue,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(14.dp),
            ambientColor = color.copy(alpha = 0.45f),
            spotColor = color.copy(alpha = 0.6f)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.88f),
            contentColor = Color.Black,
            disabledContainerColor = color.copy(alpha = 0.3f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OutcomeToggle(
    label: String,
    selected: Boolean,
    accent: Color,
    enabled: Boolean = true,
    onToggle: () -> Unit
) {
    val bg = animateColorAsState(
        if (selected) accent.copy(alpha = 0.25f) else DarkSurfaceAlt.copy(alpha = 0.5f),
        label = "outcome-bg"
    )
    val border = animateColorAsState(
        if (selected) accent else TextMuted.copy(alpha = 0.4f),
        label = "outcome-border"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 36.dp)
            .shadow(
                elevation = if (selected) 10.dp else 0.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = accent.copy(alpha = 0.35f),
                spotColor = accent.copy(alpha = 0.5f)
            )
            .background(bg.value, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onToggle() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .border(BorderStroke(1.dp, border.value), RoundedCornerShape(12.dp))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}
