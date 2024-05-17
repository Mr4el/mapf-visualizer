package gui.views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import gui.Constants
import gui.Constants.CELL_SIZE_PX
import gui.Constants.MAX_ZOOM
import gui.Constants.MIN_ZOOM
import gui.Constants.SCROLL_LAMBDA
import gui.components.elements.draggableZoomableContainer
import gui.components.elements.fpsCounter
import gui.components.mapf.*
import gui.enums.AgentColor
import gui.style.CustomColors.BACKGROUND_COLOR
import gui.utils.ColorPicker
import gui.utils.Utils.updateFps
import kotlinx.coroutines.isActive
import problem.Agent
import problem.ClassicalMapf
import problem.Obstacle
import problem.obj.Point
import problem.obj.Path
import java.lang.System.nanoTime

@Composable
@Preview
fun mapfView() {
    val classicalMapfProblem = ClassicalMapf(
        gridXSize = 10,
        gridYSize = 10,
        solutionCost = 5,
        obstacles = mutableSetOf(Obstacle(1, 2), Obstacle(3, 0)),
        agents = mutableSetOf(
            Agent(
                name = "1",
                primaryColor = AgentColor.ORANGE.primaryArgb,
                secondaryColor = AgentColor.ORANGE.secondaryArgb,
                path = Path(Point(1, 0), Point(2, 0), Point(2, 1), Point(2, 2), Point(2, 3)),
//                path = Path(Point(2, 3), Point(2, 2), Point(2, 1), Point(2, 0), Point(1, 0)),
                startPosition = Point(1, 0),
                targetPosition = Point(2, 3),
            ),
            Agent(
                name = "2",
                primaryColor = AgentColor.GREEN.primaryArgb,
                secondaryColor = AgentColor.GREEN.secondaryArgb,
                path = Path(Point(0, 3), Point(1, 3), Point(2, 3), Point(3, 3), Point(3, 2), Point(3, 1)),
//                path = Path(Point(3, 1), Point(3, 2), Point(3, 3), Point(2, 3), Point(1, 3), Point(0, 3)),
                startPosition = Point(0, 3),
                targetPosition = Point(3, 1),
            ),
            Agent(
                name = "3",
                primaryColor = AgentColor.RED.primaryArgb,
                secondaryColor = AgentColor.RED.secondaryArgb,
                path = Path(Point(2, 1), Point(1, 1), Point(0, 1), Point(0, 2)),
//                path = Path(Point(0, 2), Point(0, 1), Point(1, 1), Point(2, 1)),
                startPosition = Point(2, 1),
                targetPosition = Point(0, 2),
            ),
        ),
        colorPicker = ColorPicker(AgentColor.ORANGE, AgentColor.GREEN, AgentColor.RED)
    )

    val scaleState = mutableStateOf(Constants.DEFAULT_SCALE)
    val offsetState = remember { mutableStateOf(Offset.Zero) }
    var mapfState by remember { mutableStateOf(classicalMapfProblem) }

    var frameStartTimeNanos by remember { mutableStateOf(nanoTime()) }
    var fps by remember { mutableStateOf(0) }
    val scale by remember { scaleState }
    val scaledCellSize = (CELL_SIZE_PX * scale).dp

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frameCurrentTimeNanos ->
                updateFps(
                    frameStartTimeNanos = frameStartTimeNanos,
                    frameCurrentTimeNanos = frameCurrentTimeNanos,
                    onFpsUpdate = { fps = it },
                    onFrameStartTimeNanosUpdate = { frameStartTimeNanos = it }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BACKGROUND_COLOR)) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            draggableZoomableContainer(
                modifier = Modifier.weight(1f, false).fillMaxSize(),
                scaleState = scaleState,
                offsetState = offsetState,
                minZoom = MIN_ZOOM,
                maxZoom = MAX_ZOOM,
                scrollLambda = SCROLL_LAMBDA
            ) {
                Box {
                    mapfGrid(
                        gridXSize = mapfState.gridXSize,
                        gridYSize = mapfState.gridYSize,
                        scaledCellSize = scaledCellSize,
                        scale = scale,
                        obstacles = mapfState.obstacles,
                        onClick = { x, y ->
                            mapfState.onGridClick(x, y)
                            mapfState = mapfState.copy()
                        },
                    )

                    agentData(
                        scaledCellSize = scaledCellSize,
                        scale = scale,
                        agents = mapfState.agents,
                        currentTimeStep = mapfState.currentTimeStep,
                    )
                }
            }

            mapfNavbar(
                mapfState = mapfState,
                onUpdateState = { updatedState -> mapfState = updatedState }
            )
        }

        fpsCounter(fps, Modifier.align(Alignment.TopStart))
    }
}
