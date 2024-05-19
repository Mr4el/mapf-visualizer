package problem.solver.cbs

import exceptions.Exceptions.agentHasReachedWaitLimit
import exceptions.ReachedWaitLimitException
import problem.Agent
import problem.ClassicalMapf
import problem.obj.Path
import problem.obj.Path.Companion.getMakespan
import problem.obj.Path.Companion.getSumOfCosts
import problem.solver.obj.Conflict
import problem.solver.SolutionValidator.findFirstConflict
import problem.solver.SolutionWithCost
import problem.solver.Solver
import java.util.PriorityQueue

class CBS(
    private val mapfProblem: ClassicalMapf,
    private val singleAgentSolver: SingleAgentAStarSolver,
) : Solver(mapfProblem) {
    constructor(mapfProblem: ClassicalMapf) : this(
        mapfProblem = mapfProblem,
        singleAgentSolver = SingleAgentAStarSolver(
            graph = mapfProblem.toGraph(),
            waitingActionsCountLimit = mapfProblem.waitingActionsCountLimit,
        )
    )

    override fun solve(): SolutionWithCost? {
        val agents = mapfProblem.agentsWithPaths.keys

        val openNodes = PriorityQueue(compareBy<CTNode> { it.depth }.thenByDescending { it.solutionSumOfCosts })
        val rootNode = try {
            createRootNode(agents)
        } catch (e: RuntimeException) {
            println(e)
            return null
        }
        openNodes.add(rootNode)

        var processedStates = 0
        while (openNodes.isNotEmpty()) {
            val currentNode = openNodes.poll()
            processedStates++

            if (processedStates % 10000 == 0) println("Processed states: $processedStates")

            val conflict = currentNode.solution.findFirstConflict(
                allowVertexConflict = mapfProblem.allowVertexConflict,
                allowSwapConflict = mapfProblem.allowSwapConflict,
            ) ?: run {
                println("$processedStates states has been calculated")
                return currentNode.toSolution()
            }

            try {
                createChildNodes(currentNode, conflict).forEach { openNodes.add(it) }
            } catch (e: RuntimeException) {
                println(e)
                return null
            }
        }
        return null
    }

    private fun createRootNode(agents: Set<Agent>): CTNode {
        val solution = mutableMapOf<Agent, Path>()
        agents.forEach { agent ->
            val path = singleAgentSolver.findPath(agent, 0, emptySet(), emptySet())
            solution[agent] = path
        }

        return CTNode(
            agent = agents.first(),
            solution = solution,
            solutionSumOfCosts = solution.values.getSumOfCosts(),
            solutionMakespan = solution.values.getMakespan(),
        )
    }

    private fun createChildNodes(node: CTNode, conflict: Conflict): List<CTNode> {
        val conflictingAgents = listOf(conflict.firstAgent, conflict.secondAgent)

        return conflictingAgents.mapNotNull { agent ->
            val additionalVertexConflict = conflict.toVertexConflict()
            val additionalEdgeConflict = conflict.toEdgeConflict()

            val parentVertexConflicts = node.getVertexConflicts(agent).toSet()
            val parentEdgeConflicts = node.getEdgeConflicts(agent).toSet()

            val allVertexConflicts =
                additionalVertexConflict?.let { parentVertexConflicts + it } ?: parentVertexConflicts
            val allEdgeConflicts = additionalEdgeConflict?.let { parentEdgeConflicts + it } ?: parentEdgeConflicts

            val correctedPath = try {
                singleAgentSolver.findPath(
                    agent = agent,
                    vertexConflicts = allVertexConflicts,
                    edgeConflicts = allEdgeConflicts,
                )
            } catch (e: ReachedWaitLimitException) {
                throw agentHasReachedWaitLimit(e.limit)
            } catch (e: RuntimeException) {
//                println("$e State invalidated, but still running!")
                return@mapNotNull null
            }

            val newSolution = node.solution.toMutableMap()
            newSolution[agent] = correctedPath

            return@mapNotNull CTNode(
                parent = node,
                vertexConflict = additionalVertexConflict,
                edgeConflict = additionalEdgeConflict,
                agent = agent,
                solution = newSolution,
                solutionSumOfCosts = newSolution.values.getSumOfCosts(),
                solutionMakespan = newSolution.values.getMakespan(),
            )
        }
    }
}
