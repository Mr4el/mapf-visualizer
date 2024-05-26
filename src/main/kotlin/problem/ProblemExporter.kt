package problem

import problem.obj.Obstacle.Companion.hasObstacleAt

object ProblemExporter {
    fun ClassicalMapf.exportProblem(): String {
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

    fun ClassicalMapf.exportAgents(mapName: String): String {
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

    fun ClassicalMapf.exportSolution(mapName: String): String {
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
}
