package gui.components.mapf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import gui.components.elements.iconButton
import gui.components.elements.modeButton
import gui.components.elements.positiveNumberInputField
import gui.enums.GridMode
import gui.style.CustomColors.DARK_BACKGROUND_COLOR
import problem.ClassicalMapf
import kotlin.math.round

@Composable
fun mapfNavbar(
    mapfState: ClassicalMapf,
    onUpdateState: (ClassicalMapf) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxHeight()
            .background(DARK_BACKGROUND_COLOR)
            .width(400.dp),
        verticalArrangement = Arrangement.Center
    ) {
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
                onClick = {
                    mapfState.activeGridMode = GridMode.SET_OBSTACLES
                    onUpdateState(mapfState.copy())
                }
            )

            iconButton(
                text = "Clear all",
                icon = painterResource("icons/cross.svg"),
                width = 120.dp,
                onClick = {
                    val emptyState = ClassicalMapf()
                    emptyState.activeGridMode = mapfState.activeGridMode
                    onUpdateState(emptyState)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.width(200.dp),
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

            var sliderValue by remember { mutableStateOf(mapfState.currentTimeStep.toFloat()) }

            Slider(
                value = sliderValue,
                onValueChange = { selectedTimeStep ->
                    sliderValue = selectedTimeStep

                    val roundedValue = round(selectedTimeStep).toInt()
                    if (mapfState.currentTimeStep != roundedValue) {
                        mapfState.currentTimeStep = roundedValue
                        onUpdateState(mapfState.copy())
                    }
                },
                valueRange = 0f..5f,
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
                onSubmit = { newValue ->
                    mapfState.setGridColumns(newValue)
                    onUpdateState(mapfState.copy())
                }
            )

            positiveNumberInputField(
                label = "Row count",
                initialValue = mapfState.gridYSize,
                maxNumber = 50,
                onSubmit = { newValue ->
                    mapfState.setGridRows(newValue)
                    onUpdateState(mapfState.copy())
                }
            )
        }
    }
}
