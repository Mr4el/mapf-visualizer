package gui.components.mapf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import gui.Constants.TRANSITION_DURATION_MS
import gui.components.elements.*
import gui.enums.AvailableSolver
import gui.enums.AvailableSolver.Companion.fromName
import gui.enums.GridMode
import gui.style.CustomColors.DARK_BACKGROUND_COLOR
import problem.ClassicalMapf
import kotlin.math.round

@Composable
fun mapfSidebar(
    mapfState: ClassicalMapf,
    mapfSolver: AvailableSolver,
    isDraggableEnabled: Boolean,
    waitingForSolution: Boolean,
    onUpdateDraggableState: (Boolean) -> Unit,
    onUpdateWaitingActionsCountLimit: (Int) -> Unit,
    onUpdateAutoplaySpeed: (Int) -> Unit,
    onUpdateAllowSwapConflict: (Boolean) -> Unit,
    onUpdateAllowVertexConflict: (Boolean) -> Unit,
    onUpdateAllowUsingVisitedCellsTwice: (Boolean) -> Unit,
    onUpdateState: (ClassicalMapf) -> Unit,
    onClearAll: () -> Unit,
    onSolverSelected: (AvailableSolver) -> Unit,
    onSolveProblem: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxHeight()
            .background(DARK_BACKGROUND_COLOR)
            .width(400.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column {
                checkBox(
                    text = "Allow swap conflicts",
                    isChecked = mapfState.allowSwapConflict,
                    enabled = !waitingForSolution,
                    onCheckedChange = onUpdateAllowSwapConflict,
                )

                checkBox(
                    text = "Allow vertex conflicts",
                    isChecked = mapfState.allowVertexConflict,
                    enabled = !waitingForSolution,
                    onCheckedChange = onUpdateAllowVertexConflict,
                )

                checkBox(
                    text = "Allow using visited cells",
                    isChecked = mapfState.allowUsingVisitedCellsTwice,
                    enabled = !waitingForSolution,
                    onCheckedChange = onUpdateAllowUsingVisitedCellsTwice,
                )

                checkBox(
                    text = "Enable drag navigation",
                    isChecked = isDraggableEnabled,
                    enabled = !waitingForSolution,
                    onCheckedChange = onUpdateDraggableState,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    positiveNumberInputField(
                        label = "Max waiting operations",
                        initialValue = mapfState.waitingActionsCountLimit,
                        maxNumber = 100,
                        width = 200.dp,
                        enabled = !waitingForSolution,
                        onSubmit = onUpdateWaitingActionsCountLimit,
                    )

                    positiveNumberInputField(
                        label = "Autoplay speed (ms)",
                        initialValue = mapfState.autoplaySpeedMs.toInt(),
                        minNumber = TRANSITION_DURATION_MS,
                        maxNumber = 5000,
                        enabled = !waitingForSolution,
                        onSubmit = onUpdateAutoplaySpeed,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            modeButton(
                text = "Agents",
                painterResource("icons/agent.svg"),
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.width(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
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
                modifier = Modifier.width(150.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            positiveNumberInputField(
                label = "Column count",
                initialValue = mapfState.gridXSize,
                maxNumber = 50,
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
                enabled = !waitingForSolution,
                onSubmit = { newValue ->
                    mapfState.setGridRows(newValue)
                    onUpdateState(mapfState.copy())
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dropdownMenuComponent(
                targetName = "algorithm",
                enabled = !waitingForSolution,
                options = AvailableSolver.values().map { it.name },
                selectedOption = mapfSolver.name,
                onOptionSelected = { onSolverSelected(fromName(it)) },
            )

            simpleButton(
                text = "Solve problem",
                enabled = !waitingForSolution,
                onClick = onSolveProblem,
            )
        }
    }
}
