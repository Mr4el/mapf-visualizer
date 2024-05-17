package gui.components.elements

import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun positiveNumberInputField(
    label: String,
    initialValue: Int,
    minNumber: Int = 0,
    maxNumber: Int? = null,
    onSubmit: (Int) -> Unit,
) {
    var number by remember { mutableStateOf(0) }
    var tempInput by remember { mutableStateOf(initialValue.toString()) }

    TextField(
        value = tempInput,
        onValueChange = { tempInput = it },
        label = { Text(label) },
        modifier = Modifier
            .width(150.dp)
            .onPreviewKeyEvent {
                if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                    tempInput.toIntOrNull()?.let { newValue ->
                        number = maxNumber?.let { newValue.coerceIn(minNumber, maxNumber) }
                            ?: newValue.coerceAtLeast(minNumber)
                        tempInput = number.toString()
                        onSubmit(number)
                        true
                    } ?: false
                } else {
                    false
                }
            }
    )
}
