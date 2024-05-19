package problem.solver.cbs

import exceptions.Exceptions.validPathForAgentNotFound
import exceptions.ReachedWaitLimitException
import gui.utils.EdgeConflict
import gui.utils.VertexConflict
import problem.Agent
import problem.obj.Graph
import problem.obj.Path
import problem.obj.Point
import problem.obj.Point.Companion.distanceTo
import problem.obj.Point.Companion.equal
import java.util.*
import kotlin.math.max

class SingleAgentAStarSolver(
    private val graph: Graph,
    private val waitingActionsCountLimit: Int,
    private val allowUsingVisitedCellsTwice: Boolean,
) {
    fun findPath(
        agent: Agent,
        initialTimeStep: Int = 0,
        vertexConflicts: Set<VertexConflict> = emptySet(),
        edgeConflicts: Set<EdgeConflict> = emptySet(),
    ): Path {
        val maxConflictTimeStep = max(
            vertexConflicts.maxOfOrNull { it.second } ?: 0,
            edgeConflicts.maxOfOrNull { it.second } ?: 0
        )

        val cameFrom = HashMap<AStarVertexState, AStarVertexState>()
        val closedSet = hashSetOf<AStarVertexState>()
        val openSet = hashSetOf<AStarVertexState>()
        val queue = PriorityQueue(compareBy<AStarVertexState> { it.fScore }.thenBy { it.timeStep })

        val initialAStarVertexState = AStarVertexState(
            position = agent.startPosition,
            timeStep = initialTimeStep,
            gScore = 0,
            hScore = heuristic(agent.startPosition, agent.targetPosition),
        )
        queue.add(initialAStarVertexState)
        openSet.add(initialAStarVertexState)

        while (queue.isNotEmpty()) {
            val currentVertexState = queue.poll()

            if (currentVertexState.position.equal(agent.targetPosition)) {
                return generatePath(cameFrom, currentVertexState)
            }

            openSet.remove(currentVertexState)
            closedSet.add(currentVertexState)

            val neighbors = graph.getNeighbors(currentVertexState.position) + currentVertexState.position
            neighbors.forEach { neighborVertexPosition ->
                val nextAStarVertexState = AStarVertexState(
                    position = neighborVertexPosition,
                    timeStep = currentVertexState.timeStep + 1,
                    gScore = currentVertexState.gScore + 1,
                    hScore = heuristic(neighborVertexPosition, agent.targetPosition)
                )

                val vertexAtTimeStep = Pair(neighborVertexPosition, nextAStarVertexState.timeStep)
                val edgeAtTimeStep = Pair(
                    Pair(currentVertexState.position, neighborVertexPosition),
                    currentVertexState.timeStep
                )

                val hasVertexConflict = vertexConflicts.containsVertex(vertexAtTimeStep)
                val hasEdgeConflict = edgeConflicts.containsEdge(edgeAtTimeStep)
                // Forbidding using already visited cells
                val alreadyVisited = allowUsingVisitedCellsTwice && closedSet.contains(nextAStarVertexState)
                val freeConflictingTargetCell = nextAStarVertexState.position.equal(agent.targetPosition)
                    && nextAStarVertexState.timeStep <= maxConflictTimeStep
                if (hasVertexConflict || hasEdgeConflict || freeConflictingTargetCell || alreadyVisited) {
                    return@forEach
                }

                if (nextAStarVertexState !in openSet && nextAStarVertexState.timeStep < graph.size()) {
                    cameFrom[nextAStarVertexState] = currentVertexState
                    queue.add(nextAStarVertexState)
                    openSet.add(nextAStarVertexState)
                }
            }
        }
        throw validPathForAgentNotFound(agent.name)
    }

    private fun generatePath(
        cameFrom: Map<AStarVertexState, AStarVertexState>,
        targetPoint: AStarVertexState,
    ): Path {
        val path = mutableListOf(targetPoint.position)
        var waitingActionsCount = 0
        var currentPoint: AStarVertexState? = targetPoint

        while (currentPoint != null) {
            val previousPoint = currentPoint
            currentPoint = cameFrom[previousPoint]

            if (currentPoint != null) {
                if (previousPoint.position == currentPoint.position) {
                    waitingActionsCount++
                } else {
                    waitingActionsCount = 0
                }

                if (waitingActionsCount > waitingActionsCountLimit) {
                    throw ReachedWaitLimitException(waitingActionsCountLimit)
                }

                path.add(0, currentPoint.position)
            }
        }

        return Path(path)
    }

    // Manhattan distance
    private fun heuristic(start: Point, target: Point): Int {
        return start.distanceTo(target)
    }

    private fun Set<VertexConflict>.containsVertex(conflict: VertexConflict): Boolean {
        val (secondVertex, secondTimeStep) = conflict

        return this.any { (firstVertex, firstTimeStep) ->
            return@any firstTimeStep == secondTimeStep && firstVertex.equal(secondVertex)
        }
    }

    private fun Set<EdgeConflict>.containsEdge(conflict: EdgeConflict): Boolean {
        val (secondEdge, secondTimeStep) = conflict
        val (secondEdgeStart, secondEdgeEnd) = secondEdge

        return this.any { (firstEdge, firstTimeStep) ->
            val (firstEdgeStart, firstEdgeEnd) = firstEdge

            return@any firstTimeStep == secondTimeStep
                && (firstEdgeStart.equal(secondEdgeStart) && firstEdgeEnd.equal(secondEdgeEnd)
                || firstEdgeStart.equal(secondEdgeEnd) && firstEdgeEnd.equal(secondEdgeStart))
        }
    }
}
