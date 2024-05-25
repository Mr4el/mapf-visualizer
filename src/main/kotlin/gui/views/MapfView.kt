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
import enums.AvailableSolver
import exceptions.Exceptions.missingMapFileException
import gui.style.CustomColors.BACKGROUND_COLOR
import gui.utils.Utils.updateFps
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import problem.ClassicalMapf
import java.io.File
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import kotlin.system.measureTimeMillis

@Composable
@Preview
fun mapfView() {
    val scaleState = mutableStateOf(DEFAULT_SCALE)
    val offsetState = remember { mutableStateOf(Offset.Zero) }
    var mapfState by remember { mutableStateOf(ClassicalMapf()) }
    var mapfSolver by remember { mutableStateOf(AvailableSolver.CBS) }

    val mapfJob = remember { MutableStateFlow<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var elapsedTime by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }

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

    LaunchedEffect(mapfState.waitingForSolution) {
        if (mapfState.waitingForSolution) {
            while (mapfState.waitingForSolution) {
                elapsedTime = currentTimeMillis() - startTime
                delay(10)
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
        if (!mapfState.hasSolution()) {
            mapfState = mapfState.apply { waitingForSolution = true }
            startTime = currentTimeMillis()

            mapfJob.value?.cancel()
            mapfJob.value = coroutineScope.launch(Dispatchers.Default) {
                println("Looking for a solution...")

                val timeSpent = measureTimeMillis {
                    mapfState.solveProblem(mapfSolver)
                }.also { println("Solution took $it ms") }

                mapfState = mapfState.apply {
                    problemSolvingTimeMs = timeSpent
                    autoPlayEnabled = true
                    waitingForSolution = false
                }.copy()
            }
        }
    }

    fun abortSolving() {
        mapfJob.value?.cancel()
        startTime = 0L
        mapfState = mapfState.apply {
            waitingForSolution = false
        }
        println("The search for a solution was interrupted manually!")
    }

    fun importProblem(problem: String) {
        try {
            clearField()
            mapfState.importProblem(problem)
        } catch (e: Exception) {
            mapfState.notificationDescription = e.message
        }
        mapfState = mapfState.copy()
    }

    fun importAgents(fileDirectory: String, agents: String) {
        try {
            val mapFileContent = try {
                val mapFileName = agents.lines()[1].split("\t")[1]
                val filePath = fileDirectory + mapFileName
                File(filePath).readText()
            } catch (e: Exception) {
                throw missingMapFileException()
            }

            clearField()
            mapfState.importProblem(mapFileContent)
            mapfState.importAgents(agents)
        } catch (e: Exception) {
            mapfState.notificationDescription = e.message
        }
        mapfState = mapfState.copy()
    }

    fun importSolution(fileDirectory: String, solution: String) {
        try {
            val mapFileContent = try {
                val mapFileName = solution.lines()[3].split("\t")[1]
                val filePath = fileDirectory + mapFileName
                File(filePath).readText()
            } catch (e: Exception) {
                throw missingMapFileException()
            }

            clearField()
            mapfState.importProblem(mapFileContent)
            mapfState.importSolution(solution)
        } catch (e: Exception) {
            mapfState.notificationDescription = e.message
        }
        mapfState = mapfState.copy()
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
                problemSolvingTime = elapsedTime,
                isDraggableEnabled = isDraggableEnabled,
                waitingForSolution = mapfState.waitingForSolution,
                exceptionDescription = mapfState.exceptionDescription,
                notificationDescription = mapfState.notificationDescription,
                onUpdateAllowSwapConflict = { updateAllowSwapConflict(it) },
                onUpdateAllowVertexConflict = { updateAllowVertexConflict(it) },
                onUpdateDraggableState = { isDraggableEnabled = it },
                onUpdateWaitingActionsCountLimit = { updateWaitingActionsCountLimit(it) },
                onUpdateAutoplaySpeed = { updateAutoplaySpeed(it) },
                onUpdateState = { mapfState = it },
                onClearAll = { clearField() },
                onSolverSelected = { mapfSolver = it },
                onSolveProblem = { solveProblem() },
                onStopFindingSolution = { abortSolving() },
                onProblemImport = { importProblem(it) },
                onAgentsImport = { fileDirectory, fileContent -> importAgents(fileDirectory, fileContent) },
                onSolutionImport = { fileDirectory, fileContent -> importSolution(fileDirectory, fileContent) },
            )
        }

        fpsCounter(fps, Modifier.align(Alignment.TopStart))
    }
}
