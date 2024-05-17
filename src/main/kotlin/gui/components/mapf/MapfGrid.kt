package gui.components.mapf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
                    val cellColor = remember(obstacles) {
                        if (obstacles.hasObstacleAt(x, y)) DARK_GRAY else WHITE
                    }

                    Box(
                        modifier = Modifier
                            .size(scaledCellSize)
                            .background(cellColor)
                            .border(1.dp * scale, BLACK)
                            .clickable { onClick(x, y) }
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
