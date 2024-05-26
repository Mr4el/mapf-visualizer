package problem.solver.cbs

import exceptions.Exceptions.agentHasReachedWaitLimitException
import exceptions.ReachedWaitLimitException
import gui.utils.EdgeConflict
import gui.utils.VertexConflict
import kotlinx.coroutines.*
import problem.obj.Agent
import problem.ClassicalMapf
import problem.obj.Path
import problem.obj.Path.Companion.getMakespan
import problem.obj.Path.Companion.getSumOfCosts
import problem.solver.obj.Conflict
import problem.solver.SolutionValidator.findFirstConflict
import problem.solver.obj.SolutionWithCost
import problem.solver.obj.Solver
import java.util.PriorityQueue
import java.util.concurrent.ConcurrentHashMap

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

    override suspend fun solve(): SolutionWithCost? {
        val agents = mapfProblem.agentsWithPaths.keys

        val openNodes = PriorityQueue(compareBy<CTNode> { it.depth }.thenBy { it.solutionSumOfCosts })
        val rootNode = createRootNode(agents)
        openNodes.add(rootNode)

        var processedStatesCount = 0
        while (openNodes.isNotEmpty()) {
            yield()
            val currentNode = openNodes.poll()
            processedStatesCount++

            if (processedStatesCount % 10000 == 0) println("Processed states: $processedStatesCount")

            val conflict = currentNode.solution.findFirstConflict(
                allowVertexConflict = mapfProblem.allowVertexConflict,
                allowSwapConflict = mapfProblem.allowSwapConflict,
            ) ?: run {
                println("$processedStatesCount states has been calculated")
                return currentNode.toSolution()
            }

            createChildNodes(currentNode, conflict).forEach { openNodes.add(it) }
        }
        return null
    }

    private fun createRootNode(agents: Set<Agent>): CTNode {
        val solution = runBlocking { calculatePaths(agents) }

        return CTNode(
            agent = agents.first(),
            solution = solution,
            solutionSumOfCosts = solution.values.getSumOfCosts(),
            solutionMakespan = solution.values.getMakespan(),
        )
    }

    private suspend fun createChildNodes(
        node: CTNode,
        conflict: Conflict
    ): List<CTNode> = coroutineScope {
        val additionalVertexConflict = conflict.toVertexConflict()
        val additionalEdgeConflict = conflict.toEdgeConflict()

        val tasks = conflict.conflictingAgents.map { agent ->
            async {
                val parentVertexConflicts = node.getVertexConflicts(agent).toSet()
                val parentEdgeConflicts = node.getEdgeConflicts(agent).toSet()

                val allVertexConflicts = additionalVertexConflict
                    ?.let { parentVertexConflicts + it }
                    ?: parentVertexConflicts
                val allEdgeConflicts = additionalEdgeConflict
                    ?.let { parentEdgeConflicts + it }
                    ?: parentEdgeConflicts

                val correctedPath = try {
                    singleAgentSolver.findPath(
                        agent = agent,
                        vertexConflicts = allVertexConflicts,
                        edgeConflicts = allEdgeConflicts,
                    )
                } catch (e: ReachedWaitLimitException) {
                    throw agentHasReachedWaitLimitException(e.limit)
                } catch (e: RuntimeException) {
                    return@async null
                }

                if (correctedPath == node.solution[agent]) return@async null
                val newSolution = node.solution.toMutableMap()
                newSolution[agent] = correctedPath

                return@async CTNode(
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

        return@coroutineScope tasks.awaitAll().filterNotNull()
    }

    private suspend fun calculatePaths(
        agents: Set<Agent>,
        vertexConflicts: Set<VertexConflict> = emptySet(),
        edgeConflicts: Set<EdgeConflict> = emptySet(),
    ): MutableMap<Agent, Path> = coroutineScope {
        val solution = ConcurrentHashMap<Agent, Path>()

        val tasks = agents.map { agent ->
            async {
                val path = singleAgentSolver.findPath(
                    agent = agent,
                    initialTimeStep = 0,
                    vertexConflicts = vertexConflicts,
                    edgeConflicts = edgeConflicts
                )
                solution[agent] = path
            }
        }

        tasks.awaitAll()
        return@coroutineScope solution
    }
}
