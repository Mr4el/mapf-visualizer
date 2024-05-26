package problem

import exceptions.Exceptions
import gui.Constants
import problem.obj.Agent
import problem.obj.Obstacle
import problem.obj.Obstacle.Companion.hasObstacleAt
import problem.obj.Path
import problem.obj.Point
import problem.solver.SolutionValidator.findFirstConflict
import problem.solver.obj.SolutionWithCost

object ProblemImporter {
    fun ClassicalMapf.importProblem(problem: String) {
        val lines = problem.lines()
        if (lines.size < 5) throw Exceptions.importFileFormatDoesNotSupportExpectedException()

        if (lines[0] != "type octile") throw Exceptions.importFileFormatDoesNotSupportExpectedException()
        if (lines[3] != "map") throw Exceptions.importFileFormatDoesNotSupportExpectedException()

        val height = lines[1].substringAfter("height ").toIntOrNull()
            ?: throw Exceptions.importFileFormatDoesNotSupportExpectedException()
        val width = lines[2].substringAfter("width ").toIntOrNull()
            ?: throw Exceptions.importFileFormatDoesNotSupportExpectedException()

        if (height > Constants.GRID_SIZE_LIMIT || width > Constants.GRID_SIZE_LIMIT) {
            throw Exceptions.reachedProblemSizeLimitException(Constants.GRID_SIZE_LIMIT)
        }

        val mapLines = lines.subList(4, lines.size)
        if (mapLines.size != height + 1) throw Exceptions.invalidMapSizeException()

        val newObstacles = mutableSetOf<Obstacle>()
        for (y in mapLines.indices) {
            val line = mapLines[y]
            if (line == "") continue
            if (line.length != width) throw Exceptions.invalidMapSizeException()

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

    fun ClassicalMapf.importAgents(agentFileContent: String) {
        val lines = agentFileContent.lines()
        if (lines.isEmpty()) return

        val versionLine = lines[0]
        if (versionLine != "version 1") throw Exceptions.importFileFormatDoesNotSupportExpectedException()

        val newAgents = mutableSetOf<Agent>()
        for (line in lines.drop(1)) {
            if (line == "") continue
            val parts = line.split("\t")
            if (parts.size < 8) throw Exceptions.missingDataWhileImportingException()

            val startPosition = Point(
                x = parts[4].toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
                y = parts[5].toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
            )

            val targetPosition = Point(
                x = parts[6].toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
                y = parts[7].toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
            )

            if (
                !startPosition.isInBoundaries(gridXSize, gridYSize)
                || !targetPosition.isInBoundaries(gridXSize, gridYSize)
            ) {
                throw Exceptions.objectOutsideTheMapException()
            }

            if (obstacles.hasObstacleAt(startPosition) || obstacles.hasObstacleAt(targetPosition)) {
                throw Exceptions.obstacleAndAgentPositionConflictException()
            }

//            if (agentsWithPaths.keys.hasAgentAt(startPosition) || agentsWithPaths.keys.hasAgentAt(targetPosition)) {
//                throw agentAndAgentPositionConflictException()
//            }

            val (primaryArgb, secondaryArgb) = colorPicker.getNextColor()
            incrementLatestAgentNumber()

            newAgents.add(
                Agent(
                    name = getLatestAgentNumber().toString(),
                    primaryColor = primaryArgb,
                    secondaryColor = secondaryArgb,
                    startPosition = startPosition,
                    targetPosition = targetPosition,
                )
            )
        }

        agentsWithPaths = newAgents.associateWith { Path(it.startPosition) }.toMutableMap()
    }

    fun ClassicalMapf.importSolution(solutionFileContent: String) {
        val lines = solutionFileContent.lines()
        if (lines.isEmpty() || lines[0] != "version 1") {
            throw Exceptions.importFileFormatDoesNotSupportExpectedException()
        }

        val makespan = lines[1].substringAfter("makespan ").toIntOrNull()
            ?: throw Exceptions.missingDataWhileImportingException()
        val sumOfCosts = lines[2].substringAfter("sumOfCosts ").toIntOrNull()
            ?: throw Exceptions.missingDataWhileImportingException()

        val agentsData = lines.drop(3)
        val newAgentsWithPaths = mutableMapOf<Agent, Path>()

        for (line in agentsData) {
            if (line == "") continue
            val parts = line.split("\t")
            if (parts.size != 7) throw Exceptions.missingDataWhileImportingException()

            val startPosition = Point(
                x = parts[2].toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
                y = parts[3].toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
            )

            val targetPosition = Point(
                x = parts[4].toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
                y = parts[5].toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
            )

            if (
                !startPosition.isInBoundaries(gridXSize, gridYSize)
                || !targetPosition.isInBoundaries(gridXSize, gridYSize)
            ) {
                throw Exceptions.objectOutsideTheMapException()
            }

            if (obstacles.hasObstacleAt(startPosition) || obstacles.hasObstacleAt(targetPosition)) {
                throw Exceptions.obstacleAndAgentPositionConflictException()
            }

            val steps = parts[6].split(" ").map {
                val (x, y) = it.removeSurrounding("(", ")").split(",")
                Point(
                    x = x.toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
                    y = y.toIntOrNull() ?: throw Exceptions.missingDataWhileImportingException(),
                )
            }

            val hasInvalidPath = steps.any { step ->
                obstacles.hasObstacleAt(step)
            }
            if (hasInvalidPath) throw Exceptions.invalidAgentPathException()

            val (primaryArgb, secondaryArgb) = colorPicker.getNextColor()
            incrementLatestAgentNumber()

            val agent = Agent(
                name = getLatestAgentNumber().toString(),
                primaryColor = primaryArgb,
                secondaryColor = secondaryArgb,
                startPosition = startPosition,
                targetPosition = targetPosition
            )

            val path = Path(steps)
            newAgentsWithPaths[agent] = path
        }

        newAgentsWithPaths.findFirstConflict()?.let {
            throw Exceptions.solutionHasConflictsException()
        }

        applySolution(
            SolutionWithCost(
                solution = newAgentsWithPaths,
                sumOfCosts = sumOfCosts,
                makespan = makespan,
            )
        )
    }
}
