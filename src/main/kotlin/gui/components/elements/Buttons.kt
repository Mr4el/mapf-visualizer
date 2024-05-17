package gui.components.elements

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import gui.enums.GridMode
import gui.style.CustomColors
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun placementModeButton(
    text: String,
    currentState: GridMode,
    targetState: GridMode,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = if (currentState == targetState) {
            ButtonDefaults.buttonColors(
                backgroundColor = CustomColors.ACTIVE_BUTTON_BACKGROUND,
                contentColor = CustomColors.ACTIVE_BUTTON_TEXT,
            )
        } else {
            ButtonDefaults.buttonColors(
                backgroundColor = CustomColors.DISABLED_BUTTON_BACKGROUND,
                contentColor = CustomColors.DISABLED_BUTTON_TEXT,
            )
        }
    ) { Text(text) }
}

@Composable
fun modeButton(
    text: String,
    icon: Painter,
    currentState: GridMode,
    targetState: GridMode,
    width: Dp? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = width?.let { Modifier.width(width) } ?: Modifier,
        colors = if (currentState == targetState) {
            ButtonDefaults.buttonColors(
                backgroundColor = CustomColors.ACTIVE_BUTTON_BACKGROUND,
                contentColor = CustomColors.ACTIVE_BUTTON_TEXT,
            )
        } else {
            ButtonDefaults.buttonColors(
                backgroundColor = CustomColors.DISABLED_BUTTON_BACKGROUND,
                contentColor = CustomColors.DISABLED_BUTTON_TEXT,
            )
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = icon,
                contentDescription = text,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text)
        }
    }
}

@Composable
fun iconButton(
    text: String,
    icon: Painter,
    width: Dp? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = width?.let { Modifier.width(width) } ?: Modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = CustomColors.ACTIVE_BUTTON_BACKGROUND,
            contentColor = CustomColors.ACTIVE_BUTTON_TEXT,
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = icon,
                contentDescription = text,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text)
        }
    }
}
