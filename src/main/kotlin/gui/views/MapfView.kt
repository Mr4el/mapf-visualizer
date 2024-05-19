package gui.views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import gui.Constants.CELL_SIZE_PX
import gui.Constants.DEFAULT_SCALE
import gui.Constants.MAX_ZOOM
import gui.Constants.MIN_ZOOM
import gui.Constants.SCROLL_LAMBDA
import gui.components.elements.draggableZoomableContainer
import gui.components.elements.fpsCounter
import gui.components.mapf.*
import gui.enums.AgentColor
import gui.enums.AvailableSolver
import gui.style.CustomColors.BACKGROUND_COLOR
import gui.utils.ColorPicker
import gui.utils.Utils.updateFps
import kotlinx.coroutines.*
import problem.Agent
import problem.ClassicalMapf
import problem.Obstacle
import problem.obj.Point
import problem.obj.Path
import java.lang.System.nanoTime
import kotlin.system.measureTimeMillis

@Composable
@Preview
fun mapfView() {
    val classicalMapfProblem = ClassicalMapf(
        gridXSize = 10,
        gridYSize = 10,
        solutionCostMakespan = null,
        obstacles = mutableSetOf(Obstacle(1, 2), Obstacle(3, 0)),
        agentsWithPaths = mutableMapOf(
            Agent(
                name = "1",
                primaryColor = AgentColor.ORANGE.primaryArgb,
                secondaryColor = AgentColor.ORANGE.secondaryArgb,
                startPosition = Point(1, 0),
                targetPosition = Point(2, 3),
            ) to Path(Point(1, 0)),
//            ) to Path(Point(1, 0), Point(2, 0), Point(2, 1), Point(2, 2), Point(2, 3)),
//            ) to Path(Point(2, 3), Point(2, 2), Point(2, 1), Point(2, 0), Point(1, 0)),
            Agent(
                name = "2",
                primaryColor = AgentColor.GREEN.primaryArgb,
                secondaryColor = AgentColor.GREEN.secondaryArgb,
                startPosition = Point(0, 3),
                targetPosition = Point(3, 1),
            ) to Path(Point(0, 3)),
//            ) to Path(Point(0, 3), Point(1, 3), Point(2, 3), Point(3, 3), Point(3, 2), Point(3, 1)),
//            ) to Path(Point(3, 1), Point(3, 2), Point(3, 3), Point(2, 3), Point(1, 3), Point(0, 3)),
            Agent(
                name = "3",
                primaryColor = AgentColor.RED.primaryArgb,
                secondaryColor = AgentColor.RED.secondaryArgb,
                startPosition = Point(2, 1),
                targetPosition = Point(0, 2),
            ) to Path(Point(2, 1)),
//            ) to Path(Point(2, 1), Point(1, 1), Point(0, 1), Point(0, 2)),
//            ) to Path(Point(0, 2), Point(0, 1), Point(1, 1), Point(2, 1)),
        ),
        colorPicker = ColorPicker(AgentColor.ORANGE, AgentColor.GREEN, AgentColor.RED)
    )


    val scaleState = mutableStateOf(DEFAULT_SCALE)
    val offsetState = remember { mutableStateOf(Offset.Zero) }
    var mapfState by remember { mutableStateOf(classicalMapfProblem) }
    var mapfSolver by remember { mutableStateOf(AvailableSolver.CBS) }
    val mapfJob = remember { Job() }

    var frameStartTimeNanos by remember { mutableStateOf(nanoTime()) }
    var fps by remember { mutableStateOf(0) }
    val scale by remember { scaleState }
    val scaledCellSize = (CELL_SIZE_PX * scale).dp
    var isDraggableEnabled by remember { mutableStateOf(false) }



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

    LaunchedEffect(mapfState.autoPlayEnabled) {
        while (isActive) {
            if (mapfState.autoPlayEnabled) {
                if (!mapfState.hasNextTimeStep()) {
                    mapfState.autoPlayEnabled = false
                }

                delay(mapfState.autoplaySpeedMs)
                mapfState.nextTimeStep()

                mapfState = mapfState.copy()
            } else {
                delay(mapfState.autoplaySpeedMs / 2)
            }
        }
    }



    fun updateAllowSwapConflict(allowSwapConflict: Boolean) {
        mapfState.allowSwapConflict = allowSwapConflict
        mapfState.resetMapfSolution()
        mapfState = mapfState.copy()
    }

    fun updateAllowVertexConflict(allowVertexConflict: Boolean) {
        mapfState.allowVertexConflict = allowVertexConflict
        mapfState.resetMapfSolution()
        mapfState = mapfState.copy()
    }

    fun updateAllowUsingVisitedCellsTwice(allowUsingVisitedCellsTwice: Boolean) {
        mapfState.allowUsingVisitedCellsTwice = allowUsingVisitedCellsTwice
        mapfState.resetMapfSolution()
        mapfState = mapfState.copy()
    }

    fun updateWaitingActionsCountLimit(waitingActionsCountLimit: Int) {
        mapfState.waitingActionsCountLimit = waitingActionsCountLimit
        mapfState.resetMapfSolution()
        mapfState = mapfState.copy()
    }

    fun updateAutoplaySpeed(autoplaySpeedMs: Int) {
        mapfState.autoplaySpeedMs = autoplaySpeedMs.toLong()
        mapfState = mapfState.copy()
    }

    fun clearField() {
        val emptyState = ClassicalMapf()
        emptyState.activeGridMode = mapfState.activeGridMode
        emptyState.allowSwapConflict = mapfState.allowSwapConflict
        emptyState.allowVertexConflict = mapfState.allowVertexConflict
        emptyState.autoplaySpeedMs = mapfState.autoplaySpeedMs
        emptyState.gridXSize = mapfState.gridXSize
        emptyState.gridYSize = mapfState.gridYSize
        mapfState = emptyState
        offsetState.value = Offset.Zero
    }

    fun solveProblem() {
        mapfState.solutionCostMakespan ?: run {
            mapfState = mapfState.apply { waitingForSolution = true }
            CoroutineScope(Dispatchers.Default + mapfJob).launch {
                measureTimeMillis {
                    mapfState.solveProblem(mapfSolver)
                }.let { println("Solution took $it ms") }
                mapfState = mapfState.apply {
                    autoPlayEnabled = true
                    waitingForSolution = false
                }.copy()
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
                isDraggableEnabled = isDraggableEnabled,
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
                        agentsWithPaths = mapfState.agentsWithPaths,
                        currentTimeStep = mapfState.currentTimeStep,
                    )
                }
            }

            mapfSidebar(
                mapfState = mapfState,
                mapfSolver = mapfSolver,
                isDraggableEnabled = isDraggableEnabled,
                waitingForSolution = mapfState.waitingForSolution,
                onUpdateAllowSwapConflict = { updateAllowSwapConflict(it) },
                onUpdateAllowVertexConflict = { updateAllowVertexConflict(it) },
                onUpdateAllowUsingVisitedCellsTwice = { updateAllowUsingVisitedCellsTwice(it) },
                onUpdateDraggableState = { isDraggableEnabled = it },
                onUpdateWaitingActionsCountLimit = { updateWaitingActionsCountLimit(it) },
                onUpdateAutoplaySpeed = { updateAutoplaySpeed(it) },
                onUpdateState = { mapfState = it },
                onClearAll = { clearField() },
                onSolverSelected = { mapfSolver = it },
                onSolveProblem = { solveProblem() }
            )
        }

        fpsCounter(fps, Modifier.align(Alignment.TopStart))
    }
}
