package problem.solver

import problem.ClassicalMapf

abstract class Solver(
    val classicalMapfProblem: ClassicalMapf,
) {
    abstract suspend fun solve(): SolutionWithCost?
}
