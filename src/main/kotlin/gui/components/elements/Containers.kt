package gui.components.elements

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun draggableZoomableContainer(
    modifier: Modifier,
    scaleState: MutableState<Float>,
    offsetState: MutableState<Offset>,
    minZoom: Float,
    maxZoom: Float,
    scrollLambda: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ ->
                    offsetState.value = offsetState.value + pan
                }
            }.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { pointerInputChange ->
                            if (pointerInputChange.scrollDelta != Offset.Zero) {
                                val scrollDelta = pointerInputChange.scrollDelta
                                scaleState.value = (scaleState.value - scrollDelta.y * scrollLambda)
                                    .coerceIn(minZoom, maxZoom)
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetState.value.x.roundToInt(), offsetState.value.y.roundToInt()) }
                .scale(scaleState.value)
        ) {
            content()
        }
    }
}
