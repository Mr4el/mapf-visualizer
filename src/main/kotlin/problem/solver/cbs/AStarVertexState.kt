package problem.solver.cbs

import problem.obj.Point

data class AStarVertexState(
    val position: Point,
    val timeStep: Int,
    val gScore: Int,
    val hScore: Int,
    val fScore: Int = gScore + hScore,
)
