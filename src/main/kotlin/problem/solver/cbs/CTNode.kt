package problem.solver.cbs

import gui.utils.BasicSolution
import gui.utils.EdgeConflict
import gui.utils.VertexConflict
import problem.Agent
import problem.solver.SolutionWithCost

class CTNode(
    val agent: Agent,
    val solution: BasicSolution,
    val solutionSumOfCosts: Int,
    val solutionMakespan: Int,

    private val parent: CTNode? = null,
    private val vertexConflict: VertexConflict? = null,
    private val edgeConflict: EdgeConflict? = null,
) {
    fun toSolution() = SolutionWithCost(
        solution = solution,
        sumOfCosts = solutionSumOfCosts,
        makespan = solutionMakespan
    )

    fun getVertexConflicts(agent: Agent): Sequence<VertexConflict> = sequence {
        if (this@CTNode.agent == agent && vertexConflict != null) {
            yield(vertexConflict)
        }
        parent?.let {
            yieldAll(it.getVertexConflicts(agent))
        }
    }

    fun getEdgeConflicts(agent: Agent): Sequence<EdgeConflict> = sequence {
        if (this@CTNode.agent == agent && edgeConflict != null) {
            yield(edgeConflict)
        }
        parent?.let {
            yieldAll(it.getEdgeConflicts(agent))
        }
    }

    val depth: Int = parent?.depth?.plus(1) ?: 0
}
