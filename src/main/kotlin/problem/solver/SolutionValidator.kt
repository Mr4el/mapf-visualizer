package problem.solver

import gui.utils.BasicSolution
import problem.Agent
import problem.obj.Path.Companion.getMakespan
import problem.obj.Point

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
                this.forEach { (agent, path) ->
                    val currentPosition = path.timeStepPosition(currentTimeStep)
                    val nextPosition = path.timeStepPosition(currentTimeStep + 1)

                    this.forEach secondAgentLoop@{ (otherAgent, otherPath) ->
                        if (agent == otherAgent) return@secondAgentLoop

                        val otherCurrentPosition = otherPath.timeStepPosition(currentTimeStep)
                        val otherNextPosition = otherPath.timeStepPosition(currentTimeStep + 1)

                        // Searching for swap conflict
                        if (currentPosition == otherNextPosition && nextPosition == otherCurrentPosition) {
                            return Conflict(
                                firstAgent = agent,
                                secondAgent = otherAgent,
                                conflictFirstAgentLocation = currentPosition,
                                conflictSecondAgentLocation = otherCurrentPosition,
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
