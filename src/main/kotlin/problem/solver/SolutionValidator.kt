package problem.solver

import gui.utils.BasicSolution
import problem.Agent
import problem.obj.Path.Companion.getMakespan
import problem.obj.Point
import problem.solver.obj.Conflict

object SolutionValidator {

    fun BasicSolution.findFirstConflict(
        allowVertexConflict: Boolean,
        allowSwapConflict: Boolean,
    ): Conflict? {
        if (allowVertexConflict && allowSwapConflict) return null

        val currentStepPositions = mutableMapOf<Point, Agent>()
        val makespan = this.values.getMakespan()

        for (currentTimeStep in 0..makespan) {
            currentStepPositions.clear()

            // Searching for vertex conflicts
            if (!allowVertexConflict) {
                this.forEach { (agent, path) ->
                    val currentPosition = path.timeStepPosition(currentTimeStep)
                    currentStepPositions[currentPosition]?.let { conflictingAgent ->
                        return Conflict(
                            firstAgent = agent,
                            secondAgent = conflictingAgent,
                            conflictFirstAgentLocation = currentPosition,
                            conflictSecondAgentLocation = currentPosition,
                            timeStep = currentTimeStep,
                            conflictType = Conflict.Type.VERTEX,
                        )
                    }
                    currentStepPositions[currentPosition] = agent
                }
            }

            // Searching for edge conflicts
            if (!allowSwapConflict) {
                this.forEach { (firstAgent, firstAgentPath) ->
                    val firstAgentCurrentPosition = firstAgentPath.timeStepPosition(currentTimeStep)
                    val firstAgentNextPosition = firstAgentPath.timeStepPosition(currentTimeStep + 1)

                    this.forEach secondAgentLoop@{ (secondAgent, secondAgentPath) ->
                        if (firstAgent == secondAgent) return@secondAgentLoop

                        val secondAgentCurrentPosition = secondAgentPath.timeStepPosition(currentTimeStep)
                        val secondAgentNextPosition = secondAgentPath.timeStepPosition(currentTimeStep + 1)

                        // Searching for swap conflict
                        if (
                            firstAgentCurrentPosition == secondAgentNextPosition
                            && firstAgentNextPosition == secondAgentCurrentPosition
                        ) {
                            return Conflict(
                                firstAgent = firstAgent,
                                secondAgent = secondAgent,
                                conflictFirstAgentLocation = firstAgentCurrentPosition,
                                conflictSecondAgentLocation = secondAgentCurrentPosition,
                                timeStep = currentTimeStep,
                                conflictType = Conflict.Type.EDGE,
                            )
                        }
                    }
                }
            }
        }

        return null
    }
}
