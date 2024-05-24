package gui.components.elements

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import enums.GridMode
import gui.style.CustomColors
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun simpleButton(
    text: String,
    enabled: Boolean = true,
    width: Dp? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = width?.let { Modifier.width(width) } ?: Modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text)
        }
    }
}

@Composable
fun modeButton(
    text: String,
    icon: Painter,
    currentState: GridMode,
    targetState: GridMode,
    width: Dp? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
fun <T> expandedButton(
    icon: Painter,
    text: String,
    width: Dp? = null,
    enabled: Boolean = true,
    options: List<Pair<T, Boolean>>,
    onClick: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = !expanded },
            modifier = width?.let { Modifier.width(width) } ?: Modifier,
            enabled = enabled,
        ) { Text(text) }

        DropdownMenu(
            modifier = width?.let { Modifier.width(width) } ?: Modifier,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (option, enabled) ->
                DropdownMenuItem(
                    enabled = enabled,
                    onClick = {
                        expanded = false
                        onClick(option)
                    }
                ) {
                    Image(
                        painter = icon,
                        contentDescription = text,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(option.toString())
                }
            }
        }
    }
}

@Composable
fun iconButton(
    text: String,
    icon: Painter,
    width: Dp? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
