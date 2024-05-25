package gui.components.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun dropdownMenuComponent(
    targetName: String,
    enabled: Boolean = true,
    options: List<String>,
    width: Dp? = null,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionState by remember { mutableStateOf("$selectedOption $targetName") }

    Box {
        Button(
            modifier = width?.let { Modifier.width(it) } ?: Modifier,
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Text(selectedOptionState)
        }

        DropdownMenu(
            modifier = width?.let { Modifier.width(it) } ?: Modifier,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    selectedOptionState = "$option $targetName"
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(option)
                }
            }
        }
    }
}
