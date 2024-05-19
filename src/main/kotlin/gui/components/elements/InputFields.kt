package gui.components.elements

import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun positiveNumberInputField(
    label: String,
    initialValue: Int,
    minNumber: Int = 0,
    maxNumber: Int? = null,
    width: Dp = 150.dp,
    enabled: Boolean = true,
    onSubmit: (Int) -> Unit,
) {
    var number by remember { mutableStateOf(0) }
    var tempInput by remember { mutableStateOf(initialValue.toString()) }

    fun applyChanges() {
        tempInput.toIntOrNull()?.let { newValue ->
            number = maxNumber?.let { newValue.coerceIn(minNumber, maxNumber) }
                ?: newValue.coerceAtLeast(minNumber)
            tempInput = number.toString()
            onSubmit(number)
        }
    }

    TextField(
        value = tempInput,
        enabled = enabled,
        onValueChange = { tempInput = it },
        label = { Text(label) },
        modifier = Modifier
            .width(width)
            .onPreviewKeyEvent {
                if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                    applyChanges()
                    true
                } else {
                    false
                }
            }
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    applyChanges()
                }
            },
    )
}
