package gui.components.mapf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.Constants.TRANSITION_DURATION_MS
import gui.components.elements.*
import enums.AvailableSolver
import enums.AvailableSolver.Companion.fromName
import enums.GridMode
import gui.style.CustomColors.DARK_BACKGROUND_COLOR
import gui.utils.Utils.formatAsElapsedTime
import problem.ClassicalMapf
import kotlin.math.round

@Composable
fun mapfSidebar(
    mapfState: ClassicalMapf,
    mapfSolver: AvailableSolver,
    problemSolvingTime: Long,
    isDraggableEnabled: Boolean,
    waitingForSolution: Boolean,
    exceptionDescription: String? = null,
    onUpdateDraggableState: (Boolean) -> Unit,
    onUpdateWaitingActionsCountLimit: (Int) -> Unit,
    onUpdateAutoplaySpeed: (Int) -> Unit,
    onUpdateAllowSwapConflict: (Boolean) -> Unit,
    onUpdateAllowVertexConflict: (Boolean) -> Unit,
    onUpdateState: (ClassicalMapf) -> Unit,
    onClearAll: () -> Unit,
    onSolverSelected: (AvailableSolver) -> Unit,
    onSolveProblem: () -> Unit,
    onStopFindingSolution: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxHeight()
            .background(DARK_BACKGROUND_COLOR)
            .width(450.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.width(400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = when {
                    !mapfState.waitingForSolution && mapfState.hasSolution() -> {
                        "The solution has been found!"
                    }
                    mapfState.waitingForSolution -> "Looking for a solution..."
                    exceptionDescription != null -> "No solution found"
                    else -> "Classic MAPF visualizer"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = when {
                    !mapfState.waitingForSolution && mapfState.hasSolution() -> {
                        "Time taken: ${mapfState.problemSolvingTimeMs.formatAsElapsedTime()}"
                    }
                    mapfState.waitingForSolution -> "Time elapsed: ${problemSolvingTime.formatAsElapsedTime()}"
                    exceptionDescription != null -> exceptionDescription
                    else -> ""
                },
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.width(400.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier.width(250.dp),
            ) {
                simpleSwitch(
                    text = "Allow swap conflicts",
                    isChecked = mapfState.allowSwapConflict,
                    height = 50.dp,
                    enabled = !waitingForSolution,
                    onCheckedChange = onUpdateAllowSwapConflict,
                )

                simpleSwitch(
                    text = "Allow vertex conflicts",
                    isChecked = mapfState.allowVertexConflict,
                    height = 50.dp,
                    enabled = !waitingForSolution,
                    onCheckedChange = onUpdateAllowVertexConflict,
                )

                simpleSwitch(
                    text = "Enable drag navigation",
                    isChecked = isDraggableEnabled,
                    height = 50.dp,
                    enabled = !waitingForSolution,
                    onCheckedChange = onUpdateDraggableState,
                )
            }

            Column(
                modifier = Modifier.width(150.dp),
            ) {
                Box(
                    modifier = Modifier.height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Makespan: ${mapfState.solutionCostMakespan ?: "–"}",
                        textAlign = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier.height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sum of costs: ${mapfState.solutionConstSumOfCosts ?: "–"}",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.width(400.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            modeButton(
                text = "Agents",
                icon = painterResource("icons/agent.svg"),
                currentState = mapfState.activeGridMode,
                targetState = GridMode.SET_START_END_POINTS,
                width = 120.dp,
                enabled = !waitingForSolution,
                onClick = {
                    mapfState.activeGridMode = GridMode.SET_START_END_POINTS
                    onUpdateState(mapfState.copy())
                }
            )

            modeButton(
                text = "Obstacles",
                icon = painterResource("icons/obstacle.svg"),
                currentState = mapfState.activeGridMode,
                targetState = GridMode.SET_OBSTACLES,
                width = 120.dp,
                enabled = !waitingForSolution,
                onClick = {
                    mapfState.activeGridMode = GridMode.SET_OBSTACLES
                    onUpdateState(mapfState.copy())
                }
            )

            iconButton(
                text = "Clear all",
                icon = painterResource("icons/cross.svg"),
                width = 120.dp,
                enabled = !waitingForSolution,
                onClick = onClearAll
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.width(400.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val importOptions = listOf(
                "Problem" to !mapfState.hasSolution(),
                "Solution" to !mapfState.hasSolution(),
            )

            val exportOptions = listOf(
                "Problem" to true,
                "Solution" to mapfState.hasSolution(),
            )

            expandedButton(
                text = "Import",
                width = 190.dp,
                icon = painterResource("icons/agent.svg"),
                enabled = !waitingForSolution,
                options = importOptions,
                onClick = { option ->
                    println("Import $option")
                },
            )

            expandedButton(
                text = "Export",
                width = 190.dp,
                icon = painterResource("icons/agent.svg"),
                enabled = !waitingForSolution,
                options = exportOptions,
                onClick = { option ->
                    println("Export $option")
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.width(400.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.width(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    mapfState.prevTimeStep()
                    onUpdateState(mapfState.copy())
                }) {
                    Icon(painterResource("icons/left_arrow.svg"), contentDescription = "Previous")
                }

                IconButton(onClick = {
                    mapfState.autoPlayEnabled = !mapfState.autoPlayEnabled
                    onUpdateState(mapfState.copy())
                }) {
                    Icon(
                        painterResource(if (mapfState.autoPlayEnabled) "icons/pause.svg" else "icons/play.svg"),
                        contentDescription = "Play/Pause"
                    )
                }

                IconButton(onClick = {
                    mapfState.nextTimeStep()
                    onUpdateState(mapfState.copy())
                }) {
                    Icon(painterResource("icons/right_arrow.svg"), contentDescription = "Next")
                }
            }

            Text("${mapfState.currentTimeStep}")

            val currentTimeStep = mapfState.currentTimeStep.toDouble()
            val totalTimeSteps = mapfState.solutionCostMakespan?.toDouble() ?: 1.0
            var sliderValue = ((100.0 / totalTimeSteps) * currentTimeStep).toFloat()
            Slider(
                value = sliderValue,
                onValueChange = { selectedSliderValue ->
                    sliderValue = selectedSliderValue

                    val roundedValue = round(
                        (mapfState.solutionCostMakespan?.toDouble() ?: 1.0) * selectedSliderValue / 100
                    ).toInt()
                    if (mapfState.currentTimeStep != roundedValue) {
                        mapfState.currentTimeStep = roundedValue
                        onUpdateState(mapfState.copy())
                    }
                },
                valueRange = 0f..100f,
                modifier = Modifier.width(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.width(400.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            positiveNumberInputField(
                label = "Max waiting operations",
                initialValue = mapfState.waitingActionsCountLimit,
                maxNumber = 100,
                width = 190.dp,
                enabled = !waitingForSolution,
                onSubmit = { newValue ->
                    if (mapfState.waitingActionsCountLimit != newValue) {
                        onUpdateWaitingActionsCountLimit(newValue)
                    }
                },
            )

            positiveNumberInputField(
                label = "Autoplay speed (ms)",
                initialValue = mapfState.autoplaySpeedMs.toInt(),
                minNumber = TRANSITION_DURATION_MS,
                maxNumber = 5000,
                width = 190.dp,
                enabled = !waitingForSolution,
                onSubmit = onUpdateAutoplaySpeed,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.width(400.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            positiveNumberInputField(
                label = "Column count",
                initialValue = mapfState.gridXSize,
                maxNumber = 50,
                width = 190.dp,
                enabled = !waitingForSolution,
                onSubmit = { newValue ->
                    mapfState.setGridColumns(newValue)
                    onUpdateState(mapfState.copy())
                }
            )

            positiveNumberInputField(
                label = "Row count",
                initialValue = mapfState.gridYSize,
                maxNumber = 50,
                width = 190.dp,
                enabled = !waitingForSolution,
                onSubmit = { newValue ->
                    mapfState.setGridRows(newValue)
                    onUpdateState(mapfState.copy())
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.width(400.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dropdownMenuComponent(
                targetName = "algorithm",
                enabled = !waitingForSolution,
                options = AvailableSolver.values().map { it.name },
                width = 190.dp,
                selectedOption = mapfSolver.name,
                onOptionSelected = { onSolverSelected(fromName(it)) },
            )

            simpleButton(
                text = "Solve",
                width = 85.dp,
                enabled = !waitingForSolution,
                onClick = onSolveProblem,
            )

            simpleButton(
                text = "Stop",
                width = 85.dp,
                enabled = waitingForSolution,
                onClick = onStopFindingSolution,
            )
        }
    }
}
