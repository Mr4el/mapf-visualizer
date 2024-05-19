package problem.solver

import gui.utils.EdgeConflict
import gui.utils.VertexConflict
import problem.Agent
import problem.obj.Point

data class Conflict(
    val firstAgent: Agent,
    val secondAgent: Agent,
    val conflictFirstAgentLocation: Point,
    val conflictSecondAgentLocation: Point,
    val timeStep: Int,
    val conflictType: Type,
) {
    enum class Type {
        VERTEX,
        EDGE,
    }

    fun isVertexConflict() = conflictType == Type.VERTEX
    fun isEdgeConflict() = conflictType == Type.EDGE

    fun toVertexConflict(): VertexConflict? {
        return if (isVertexConflict()) {
            Pair(conflictFirstAgentLocation, timeStep)
        } else null
    }

    fun toEdgeConflict(): EdgeConflict? {
        return if(isEdgeConflict()) {
            Pair(Pair(conflictFirstAgentLocation, conflictSecondAgentLocation), timeStep)
        } else null
    }
}
