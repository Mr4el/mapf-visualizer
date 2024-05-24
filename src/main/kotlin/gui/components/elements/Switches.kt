package gui.components.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gui.style.CustomColors.CHECKED_THUMB_COLOR

@Composable
fun simpleSwitch(
    text: String,
    isChecked: Boolean,
    height: Dp? = null,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    var isCheckedState by remember { mutableStateOf(isChecked) }

    Row(
        modifier = height?.let { Modifier.height(height) } ?: Modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            enabled = enabled,
            checked = isCheckedState,
            onCheckedChange = { updatedValue ->
                isCheckedState = updatedValue
                onCheckedChange(updatedValue)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = CHECKED_THUMB_COLOR,
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}