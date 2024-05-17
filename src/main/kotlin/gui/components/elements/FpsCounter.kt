package gui.components.elements

import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import androidx.compose.ui.unit.sp
import gui.style.CustomColors.BLACK
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun fpsCounter(fpsValue: Int, modifier: Modifier = Modifier) {
    Text(
        text = "FPS: $fpsValue",
        fontSize = 16.sp,
        color = BLACK,
        modifier = modifier.padding(16.dp)
    )
}
