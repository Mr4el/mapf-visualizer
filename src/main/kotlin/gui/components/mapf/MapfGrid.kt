package gui.components.mapf

import androidx.compose.foundation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gui.style.CustomColors.BLACK
import gui.style.CustomColors.DARK_GRAY
import gui.style.CustomColors.WHITE
import problem.Obstacle
import problem.Obstacle.Companion.hasObstacleAt

@Composable
fun mapfGrid(
    gridXSize: Int,
    gridYSize: Int,
    scaledCellSize: Dp,
    scale: Float,
    obstacles: Set<Obstacle>,
    onClick: (Int, Int) -> Unit,
) {
    LazyColumn(userScrollEnabled = false) {
        items(gridYSize) { y ->
            LazyRow(userScrollEnabled = false) {
                items(gridXSize) { x ->
                    val cellColor = if (obstacles.hasObstacleAt(x, y)) DARK_GRAY else WHITE
                    val animatedCellColor by animateColorAsState(targetValue = cellColor)

                    Box(
                        modifier = Modifier
                            .size(scaledCellSize)
                            .background(animatedCellColor)
                            .border(1.dp * scale, BLACK)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onClick(x, y) }
                                )
                            }
                    )
                }
            }
        }
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

//@Composable
//fun mapfGridTest(
//    gridXSize: Int,
//    gridYSize: Int,
//    scaledCellSize: Dp,
//    scale: Float,
//    obstacles: Set<Obstacle>,
//    onClick: (Int, Int) -> Unit,
//) {
//    LazyVerticalGrid(
//        userScrollEnabled = false,
//        columns = GridCells.Fixed(gridXSize),
//    ) {
//        items(gridYSize * gridXSize) { index ->
//            val y = index / gridXSize
//            val x = index % gridXSize
//
//            val cellColor =  if (obstacles.hasObstacleAt(x, y)) DARK_GRAY else WHITE
//
//            Box(
//                modifier = Modifier
//                    .size(scaledCellSize)
//                    .background(cellColor)
//                    .border(1.dp * scale, BLACK)
//                    .clickable { onClick(x, y) }
//            )
//        }
//    }
//}