package problem

import enums.AvailableSolver
import enums.GridMode
import exceptions.Exceptions.importFileFormatDoesNotSupportExpectedException
import exceptions.Exceptions.invalidAgentPathException
import exceptions.Exceptions.invalidMapSizeException
import exceptions.Exceptions.missingDataWhileImportingException
import exceptions.Exceptions.objectOutsideTheMapException
import exceptions.Exceptions.obstacleAndAgentPositionConflictException
import exceptions.Exceptions.reachedProblemSizeLimitException
import exceptions.Exceptions.solutionHasConflictsException
import gui.Constants.GRID_SIZE_LIMIT
import gui.utils.ColorPicker
import gui.utils.MutableBasicSolution
import kotlinx.coroutines.CancellationException
import problem.Agent.Companion.hasAgentAt
import problem.Obstacle.Companion.hasObstacleAt
import problem.obj.Graph
import problem.obj.Path
import problem.obj.Point
import problem.obj.Point.Companion.atOrAfter
import problem.solver.SolutionValidator.findFirstConflict
import problem.solver.SolutionWithCost
import problem.solver.cbs.CBS

data class ClassicalMapf(
    var gridXSize: Int = 10,
    var gridYSize: Int = 10,

    var activeGridMode: GridMode = GridMode.SET_OBSTACLES,
    var autoPlayEnabled: Boolean = false,
    var autoplaySpeedMs: Long = 250,
    var waitingForSolution: Boolean = false,
    var problemSolvingTimeMs: Long = 0,
    var exceptionDescription: String? = null,
    var notificationDescription: String? = null,

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
    fun hasSolution(): Boolean {
        return solutionCostMakespan != null || solutionConstSumOfCosts != null
    }

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

    suspend fun solveProblem(selectedSolver: AvailableSolver) {
        val solver = when (selectedSolver) {
            AvailableSolver.CBS -> CBS(this)
        }

        try {
            solver.solve()?.let { applySolution(it) }
        } catch (e: CancellationException) {
            exceptionDescription = "The search for a solution has been canceled!"
        } catch (e: Exception) {
            exceptionDescription = e.message
        }
    }

    fun resetMapfSolution() {
        agentsWithPaths.forEach { (agent, path) ->
            agent.showPath = true
            path.resetPath(agent.startPosition)
        }
        solutionCostMakespan = null
        solutionConstSumOfCosts = null
        exceptionDescription = null
        notificationDescription = null
        resetTimeStep()
    }

    fun importProblem(problem: String) {
        val lines = problem.lines()
        if (lines.size < 5) throw importFileFormatDoesNotSupportExpectedException()

        if (lines[0] != "type octile") throw importFileFormatDoesNotSupportExpectedException()
        if (lines[3] != "map") throw importFileFormatDoesNotSupportExpectedException()

        val height = lines[1].substringAfter("height ").toIntOrNull()
            ?: throw importFileFormatDoesNotSupportExpectedException()
        val width = lines[2].substringAfter("width ").toIntOrNull()
            ?: throw importFileFormatDoesNotSupportExpectedException()

        if (height > GRID_SIZE_LIMIT || width > GRID_SIZE_LIMIT) {
            throw reachedProblemSizeLimitException(GRID_SIZE_LIMIT)
        }

        val mapLines = lines.subList(4, lines.size)
        if (mapLines.size != height + 1) throw invalidMapSizeException()

        val newObstacles = mutableSetOf<Obstacle>()
        for (y in mapLines.indices) {
            val line = mapLines[y]
            if (line == "") continue
            if (line.length != width) throw invalidMapSizeException()

            for (x in line.indices) {
                when (line[x]) {
                    '@' -> newObstacles.add(Obstacle(x, y))
                    '.' -> {}

                    // Does not support other type of obstacles
                    'T' -> newObstacles.add(Obstacle(x, y))
                    'E' -> {}
                    'S' -> {}
                }
            }
        }

        obstacles.clear()
        setGridColumns(width)
        setGridRows(height)
        obstacles.addAll(newObstacles)
    }

    fun importAgents(agentFileContent: String) {
        val lines = agentFileContent.lines()
        if (lines.isEmpty()) return

        val versionLine = lines[0]
        if (versionLine != "version 1") throw importFileFormatDoesNotSupportExpectedException()

        val newAgents = mutableSetOf<Agent>()
        for (line in lines.drop(1)) {
            if (line == "") continue
            val parts = line.split("\t")
            if (parts.size < 8) throw missingDataWhileImportingException()

            val startPosition = Point(
                x = parts[4].toIntOrNull() ?: throw missingDataWhileImportingException(),
                y = parts[5].toIntOrNull() ?: throw missingDataWhileImportingException(),
            )

            val targetPosition = Point(
                x = parts[6].toIntOrNull() ?: throw missingDataWhileImportingException(),
                y = parts[7].toIntOrNull() ?: throw missingDataWhileImportingException(),
            )

            if (
                !startPosition.isInBoundaries(gridXSize, gridYSize)
                || !targetPosition.isInBoundaries(gridXSize, gridYSize)
            ) {
                throw objectOutsideTheMapException()
            }

            if (obstacles.hasObstacleAt(startPosition) || obstacles.hasObstacleAt(targetPosition)) {
                throw obstacleAndAgentPositionConflictException()
            }

//            if (agentsWithPaths.keys.hasAgentAt(startPosition) || agentsWithPaths.keys.hasAgentAt(targetPosition)) {
//                throw agentAndAgentPositionConflictException()
//            }

            val (primaryArgb, secondaryArgb) = colorPicker.getNextColor()
            latestAgentNumber++

            newAgents.add(
                Agent(
                    name = latestAgentNumber.toString(),
                    primaryColor = primaryArgb,
                    secondaryColor = secondaryArgb,
                    startPosition = startPosition,
                    targetPosition = targetPosition,
                )
            )
        }

        agentsWithPaths = newAgents.associateWith { Path(it.startPosition) }.toMutableMap()
    }

    fun importSolution(solutionFileContent: String) {
        val lines = solutionFileContent.lines()
        if (lines.isEmpty() || lines[0] != "version 1") {
            throw importFileFormatDoesNotSupportExpectedException()
        }

        val makespan = lines[1].substringAfter("makespan ").toIntOrNull()
            ?: throw missingDataWhileImportingException()
        val sumOfCosts = lines[2].substringAfter("sumOfCosts ").toIntOrNull()
            ?: throw missingDataWhileImportingException()

        val agentsData = lines.drop(3)
        val newAgentsWithPaths = mutableMapOf<Agent, Path>()

        for (line in agentsData) {
            if (line == "") continue
            val parts = line.split("\t")
            if (parts.size != 7) throw missingDataWhileImportingException()

            val startPosition = Point(
                x = parts[2].toIntOrNull() ?: throw missingDataWhileImportingException(),
                y = parts[3].toIntOrNull() ?: throw missingDataWhileImportingException(),
            )

            val targetPosition = Point(
                x = parts[4].toIntOrNull() ?: throw missingDataWhileImportingException(),
                y = parts[5].toIntOrNull() ?: throw missingDataWhileImportingException(),
            )

            if (
                !startPosition.isInBoundaries(gridXSize, gridYSize)
                || !targetPosition.isInBoundaries(gridXSize, gridYSize)
            ) {
                throw objectOutsideTheMapException()
            }

            if (obstacles.hasObstacleAt(startPosition) || obstacles.hasObstacleAt(targetPosition)) {
                throw obstacleAndAgentPositionConflictException()
            }

            val steps = parts[6].split(" ").map {
                val (x, y) = it.removeSurrounding("(", ")").split(",")
                Point(
                    x = x.toIntOrNull() ?: throw missingDataWhileImportingException(),
                    y = y.toIntOrNull() ?: throw missingDataWhileImportingException(),
                )
            }

            val hasInvalidPath = steps.any { step ->
                obstacles.hasObstacleAt(step)
            }
            if (hasInvalidPath) throw invalidAgentPathException()

            val (primaryArgb, secondaryArgb) = colorPicker.getNextColor()
            latestAgentNumber++

            val agent = Agent(
                name = latestAgentNumber.toString(),
                primaryColor = primaryArgb,
                secondaryColor = secondaryArgb,
                startPosition = startPosition,
                targetPosition = targetPosition
            )

            val path = Path(steps)
            newAgentsWithPaths[agent] = path
        }

        newAgentsWithPaths.findFirstConflict()?.let {
            throw solutionHasConflictsException()
        }

        applySolution(
            SolutionWithCost(
                solution = newAgentsWithPaths,
                sumOfCosts = sumOfCosts,
                makespan = makespan,
            )
        )
    }

    fun exportProblem(): String {
        val sb = StringBuilder()
        sb.append("type octile\n")
        sb.append("height $gridYSize\n")
        sb.append("width $gridXSize\n")
        sb.append("map\n")
        for (y in 0 until gridYSize) {
            for (x in 0 until gridXSize) {
                sb.append(
                    when {
                        obstacles.hasObstacleAt(x, y) -> '@'
                        else -> '.'
                    }
                )
            }
            sb.append('\n')
        }
        return sb.toString()
    }

    fun exportAgents(mapName: String): String {
        val sb = StringBuilder()
        sb.append("version 1\n")
        agentsWithPaths.keys.forEachIndexed { index, agent ->
            val startX = agent.startPosition.x
            val startY = agent.startPosition.y
            val targetX = agent.targetPosition.x
            val targetY = agent.targetPosition.y

            sb.append(
                "$index\t$mapName\t$gridXSize\t$gridYSize\t" +
                    "$startX\t$startY\t$targetX\t$targetY\t" +
                    "%.8f".format(0f) + "\n"
            )
        }
        return sb.toString()
    }

    fun exportSolution(mapName: String): String {
        val sb = StringBuilder()
        sb.append("version 1\n")
        sb.append("makespan $solutionCostMakespan\n")
        sb.append("sumOfCosts $solutionConstSumOfCosts\n")

        agentsWithPaths.keys.forEachIndexed { index, agent ->
            val startX = agent.startPosition.x
            val startY = agent.startPosition.y
            val endX = agent.targetPosition.x
            val endY = agent.targetPosition.y

            val stepsString = agentsWithPaths[agent]?.let { path ->
                path.steps.joinToString(" ") { it.toString() }
            } ?: agent.startPosition.toString()

            sb.append("$index\t$mapName\t$startX\t$startY\t$endX\t$endY\t$stepsString\n")
        }
        return sb.toString()
    }

    private fun applySolution(solution: SolutionWithCost) {
        agentsWithPaths = solution.solution.toMutableMap()
        solutionCostMakespan = solution.makespan
        solutionConstSumOfCosts = solution.sumOfCosts
        exceptionDescription = null
        notificationDescription = null
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
