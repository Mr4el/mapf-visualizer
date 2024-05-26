package problem.solver.cbs

import gui.utils.BasicSolution
import gui.utils.EdgeConflict
import gui.utils.VertexConflict
import problem.obj.Agent
import problem.solver.obj.SolutionWithCost

class CTNode(
    val agent: Agent,
    val solution: BasicSolution,
    val solutionSumOfCosts: Int,
    val solutionMakespan: Int,

    private val parent: CTNode? = null,
    private val vertexConflict: VertexConflict? = null,
    private val edgeConflict: EdgeConflict? = null,
) {
    val depth: Int = (parent?.depth ?: 0) + 1

    fun toSolution() = SolutionWithCost(
        solution = solution,
        sumOfCosts = solutionSumOfCosts,
        makespan = solutionMakespan
    )

    fun getVertexConflicts(agent: Agent): List<VertexConflict> {
        val conflicts = mutableListOf<VertexConflict>()
        var current: CTNode? = this
        while (current != null) {
            if (current.agent == agent && current.vertexConflict != null) {
                 conflicts.add(current.vertexConflict!!)
            }
            current = current.parent
        }
        return conflicts
    }

    fun getEdgeConflicts(agent: Agent): List<EdgeConflict> {
        val conflicts = mutableListOf<EdgeConflict>()
        var current: CTNode? = this
        while (current != null) {
            if (current.agent == agent && current.edgeConflict != null) {
                conflicts.add(current.edgeConflict!!)
            }
            current = current.parent
        }
        return conflicts
    }
}
