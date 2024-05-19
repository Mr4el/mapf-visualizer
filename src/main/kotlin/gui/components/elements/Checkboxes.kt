package gui.components.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun checkBox(
    text: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    var isCheckedState by remember { mutableStateOf(isChecked) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            enabled = enabled,
            checked = isCheckedState,
            onCheckedChange = { updatedValue ->
                isCheckedState = updatedValue
                onCheckedChange(updatedValue)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}