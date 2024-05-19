package problem

import gui.enums.AvailableSolver
import gui.enums.GridMode
import gui.utils.ColorPicker
import gui.utils.MutableBasicSolution
import problem.Agent.Companion.hasAgentAt
import problem.Obstacle.Companion.hasObstacleAt
import problem.obj.Graph
import problem.obj.Path
import problem.obj.Point
import problem.obj.Point.Companion.atOrAfter
import problem.solver.SolutionWithCost
import problem.solver.cbs.CBS

data class ClassicalMapf(
    var gridXSize: Int = 10,
    var gridYSize: Int = 10,

    var activeGridMode: GridMode = GridMode.SET_OBSTACLES,
    var autoPlayEnabled: Boolean = false,
    var autoplaySpeedMs: Long = 250,
    var waitingForSolution: Boolean = false,

    var waitingActionsCountLimit: Int = 10,
    var allowVertexConflict: Boolean = false,
    var allowSwapConflict: Boolean = false,

    val obstacles: MutableSet<Obstacle> = mutableSetOf(),
    var agentsWithPaths: MutableBasicSolution = mutableMapOf(),
    var solutionConstSumOfCosts: Int? = null,
    var solutionCostMakespan: Int? = null,
    var currentTimeStep: Int = 0,

    val colorPicker: ColorPicker = ColorPicker(),
    private var placeTargetPointToAgent: Agent? = null,
    private var latestAgentNumber: Int = agentsWithPaths.size,
) {
    fun hasNextTimeStep(): Boolean {
        return currentTimeStep != solutionCostMakespan
    }

    fun prevTimeStep() {
        solutionCostMakespan?.let {
            currentTimeStep = (currentTimeStep - 1).coerceIn(0, solutionCostMakespan)
        }
    }

    fun nextTimeStep() {
        solutionCostMakespan?.let {
            currentTimeStep = (currentTimeStep + 1).coerceIn(0, solutionCostMakespan)
        }
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

    fun toGraph(): Graph {
        val graph = Graph()
        for (x in 0 until gridXSize) {
            for (y in 0 until gridYSize) {
                val point = Point(x, y)
                if (!obstacles.hasObstacleAt(x, y)) {
                    graph.addVertex(point)

                    val neighbors = listOf(
                        Point(x - 1, y),
                        Point(x + 1, y),
                        Point(x, y - 1),
                        Point(x, y + 1)
                    )
                    for (neighbor in neighbors) {
                        val isInGridBoundaries = neighbor.isInBoundaries(gridXSize, gridYSize)
                        if (isInGridBoundaries && !obstacles.hasObstacleAt(neighbor.x, neighbor.y)) {
                            graph.addEdge(point, neighbor)
                        }
                    }
                }
            }
        }
        return graph
    }

    fun solveProblem(selectedSolver: AvailableSolver) {

        val solver = when(selectedSolver) {
            AvailableSolver.CBS -> CBS(this)
        }

        solver.solve()?.let { applySolution(it) }
    }

    fun resetMapfSolution() {
        agentsWithPaths.forEach { (agent, path) ->
            agent.showPath = true
            path.resetPath(agent.startPosition)
        }
        solutionCostMakespan = null
        resetTimeStep()
    }

    private fun applySolution(solution: SolutionWithCost) {
        agentsWithPaths = solution.solution.toMutableMap()
        solutionCostMakespan = solution.makespan
        solutionConstSumOfCosts = solution.sumOfCosts
    }

    private fun updateObstacles(x: Int, y: Int) {
        if (agentsWithPaths.keys.hasAgentAt(x, y)) {
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

        placeTargetPointToAgent?.let { agentToUpdateTargetPoint ->
            if (agentsWithPaths.keys.hasAgentAt(x, y)) {
                return
            } else {
                agentToUpdateTargetPoint.targetPosition = Point(x, y)
            }
        } ?: run {
            if (agentsWithPaths.keys.hasAgentAt(x, y)) {
                agentsWithPaths.keys.firstOrNull { it.isAt(x, y) }?.let { agentToRemove ->
                    colorPicker.freeColor(agentToRemove.primaryColor)
                    colorPicker.freeColor(agentToRemove.secondaryColor)
                    agentsWithPaths.remove(agentToRemove)
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
                agentsWithPaths[createdAgent] = Path(Point(x, y))
                placeTargetPointToAgent = createdAgent
                return
            }
        }
        placeTargetPointToAgent = null
    }

    private fun removeObjectsOutsideTheGrid(x: Int = gridXSize, y: Int = gridYSize) {
        obstacles.removeIf { it.atOrAfter(x, y) }
        val agentsToRemove = agentsWithPaths.keys.filter { agent ->
            agent.startPosition.atOrAfter(x, y) || agent.targetPosition.atOrAfter(x, y)
        }.toSet()

        agentsToRemove.forEach { agentToRemove ->
            colorPicker.freeColor(agentToRemove.primaryColor)
            colorPicker.freeColor(agentToRemove.secondaryColor)
        }
        agentsWithPaths.keys.removeAll(agentsToRemove)
    }

    private fun resetTimeStep() {
        currentTimeStep = 0
    }
}
