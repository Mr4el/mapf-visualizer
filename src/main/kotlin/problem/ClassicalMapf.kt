package problem

import gui.enums.GridMode
import gui.utils.ColorPicker
import problem.Agent.Companion.hasAgentAt
import problem.Obstacle.Companion.hasObstacleAt
import problem.obj.Point
import problem.obj.Point.Companion.atOrAfter

data class ClassicalMapf(
    var gridXSize: Int = 10,
    var gridYSize: Int = 10,
    var activeGridMode: GridMode = GridMode.SET_OBSTACLES,

    var solutionCost: Int? = null,
    var currentTimeStep: Int = 0,
    var autoPlayEnabled: Boolean = false,

    val obstacles: MutableSet<Obstacle> = mutableSetOf(),
    val agents: MutableSet<Agent> = mutableSetOf(),

    val colorPicker: ColorPicker = ColorPicker(),

    private var placeTargetPointToAgent: Agent? = null,
    private var latestAgentNumber: Int = agents.size,
) {
    fun prevTimeStep() {
        solutionCost?.let {
            currentTimeStep = (currentTimeStep - 1).coerceIn(0, solutionCost)
        }
    }

    fun nextTimeStep() {
        solutionCost?.let {
            currentTimeStep = (currentTimeStep + 1).coerceIn(0, solutionCost)
        }
    }

    fun resetTimeStep() {
        currentTimeStep = 0
    }

    fun setGridColumns(columnCount: Int) {
        if (columnCount != gridXSize) {
            resetMapfSolution()
            gridXSize = columnCount
            removeObjectsOutsideTheGrid()
        }
    }

    fun setGridRows(rowCount: Int) {
        if (rowCount != gridYSize) {
            resetMapfSolution()
            gridYSize = rowCount
            removeObjectsOutsideTheGrid()
        }
    }

    fun onGridClick(x: Int, y: Int) {
        when (activeGridMode) {
            GridMode.SET_OBSTACLES -> updateObstacles(x, y)
            GridMode.SET_START_END_POINTS -> updateAgents(x, y)
        }
    }

    private fun updateObstacles(x: Int, y: Int) {
        if (agents.hasAgentAt(x, y)) {
            return
        }

        resetMapfSolution()

        if (obstacles.hasObstacleAt(x, y)) {
            obstacles.removeIf { it.isAt(x, y) }
        } else {
            obstacles.add(Obstacle(x, y))
        }
    }

    private fun updateAgents(x: Int, y: Int) {
        if (obstacles.hasObstacleAt(x, y)) {
            return
        }

        resetMapfSolution()

        placeTargetPointToAgent?.let {
            if (agents.hasAgentAt(x, y)) {
                return
            } else {
                it.targetPosition = Point(x, y)
            }
        } ?: run {
            if (agents.hasAgentAt(x, y)) {
                agents.firstOrNull { it.isAt(x, y) }?.let { agentToRemove ->
                    colorPicker.freeColor(agentToRemove.primaryColor)
                    colorPicker.freeColor(agentToRemove.secondaryColor)
                    agents.remove(agentToRemove)
                }
            } else {
                val (primaryArgb, secondaryArgb) = colorPicker.getNextColor()
                latestAgentNumber++

                val createdAgent = Agent(
                    name = (latestAgentNumber).toString(),
                    primaryColor = primaryArgb,
                    secondaryColor = secondaryArgb,
                    startPosition = Point(x, y),
                    targetPosition = Point(x, y),
                )
                agents.add(createdAgent)
                placeTargetPointToAgent = createdAgent
                return
            }
        }
        placeTargetPointToAgent = null
    }

    private fun removeObjectsOutsideTheGrid(x: Int = gridXSize, y: Int = gridYSize) {
        obstacles.removeIf { it.atOrAfter(x, y) }
        val agentsToRemove = agents.filter {
            it.startPosition.atOrAfter(x, y) || it.targetPosition.atOrAfter(x, y)
        }.toSet()

        agentsToRemove.forEach { agentToRemove ->
            colorPicker.freeColor(agentToRemove.primaryColor)
            colorPicker.freeColor(agentToRemove.secondaryColor)
        }
        agents.removeAll(agentsToRemove)
    }

    private fun resetMapfSolution() {
        agents.forEach { it.clearPath() }
        solutionCost = null
        resetTimeStep()
    }
}
