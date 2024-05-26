package gui.components.mapf

import androidx.compose.foundation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gui.style.CustomColors.BLACK
import gui.style.CustomColors.DARKER_GRAY
import gui.style.CustomColors.DARK_GRAY
import gui.style.CustomColors.LIGHTER_GRAY
import gui.style.CustomColors.WHITE
import problem.obj.Obstacle
import problem.obj.Obstacle.Companion.hasObstacleAt
import problem.obj.Point

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun mapfGrid(
    gridXSize: Int,
    gridYSize: Int,
    scale: Float,
    scaledCellSize: Dp,
    obstacles: Set<Obstacle>,
    onClick: (Int, Int) -> Unit,
) {
    var hoveredCell by remember { mutableStateOf<Point?>(null) }

    LazyColumn(userScrollEnabled = false) {
        items(gridYSize) { y ->
            LazyRow(userScrollEnabled = false) {
                items(gridXSize) { x ->
                    val backgroundColor = getBackgroundColor(
                        isObstacle = obstacles.hasObstacleAt(x, y),
                        isHovered = hoveredCell?.isAt(x, y) ?: false,
                    )
                    val animatedCellColor by animateColorAsState(
                        targetValue = backgroundColor
                    )

                    Box(modifier = Modifier
                        .size(scaledCellSize)
                        .background(animatedCellColor)
                        .border(1.dp * scale, BLACK)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onClick(x, y) })
                        }
                        .hoverable(
                            remember { MutableInteractionSource() }
                        )
                        .onPointerEvent(PointerEventType.Enter) {
                            hoveredCell = Point(x, y)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            hoveredCell = null
                        }
                    )
                }
            }
        }
    }
}

fun getBackgroundColor(isObstacle: Boolean, isHovered: Boolean): Color {
    return when {
        isObstacle && isHovered -> DARKER_GRAY
        isObstacle -> DARK_GRAY
        isHovered -> LIGHTER_GRAY
        else -> WHITE
    }
}

//@Composable
//fun mapfGridTest(
//    gridXSize: Int,
//    gridYSize: Int,
//    scaledCellSize: Dp,
//    scaleState: MutableState<Float>,
//    obstacles: Set<Obstacle>,
//    onClick: (Int, Int) -> Unit,
//) {
//    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
//        Canvas(modifier = Modifier
//            .size(width = 5000.dp, height = 5000.dp)
//            .fillMaxWidth()
//            .pointerInput(Unit) {
//                detectTapGestures { offset ->
//                    val x = (offset.x / (scaledCellSize.toPx() * scaleState.value)).toInt()
//                    val y = (offset.y / (scaledCellSize.toPx() * scaleState.value)).toInt()
//                    if (x in 0 until gridXSize && y in 0 until gridYSize) {
//                        onClick(x, y)
//                    }
//                }
//            }
//        ) {
//            for (y in 0 until gridYSize) {
//                for (x in 0 until gridXSize) {
//                    val cellColor = if (obstacles.hasObstacleAt(x, y)) Color.DarkGray else Color.White
//                    drawRect(
//                        color = cellColor,
//                        topLeft = Offset(x * scaledCellSize.toPx(), y * scaledCellSize.toPx()),
//                        size = Size(scaledCellSize.toPx(), scaledCellSize.toPx())
//                    )
//                    drawRect(
//                        color = Color.Black,
//                        topLeft = Offset(x * scaledCellSize.toPx(), y * scaledCellSize.toPx()),
//                        size = Size(scaledCellSize.toPx(), scaledCellSize.toPx()),
//                        style = Stroke(width = 1.dp.toPx())
//                    )
//                }
//            }
//        }
//    }
//}
