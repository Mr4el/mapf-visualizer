package problem.solver

import gui.utils.BasicSolution

data class SolutionWithCost(
    val solution: BasicSolution,
    val sumOfCosts: Int,
    val makespan: Int,
)
