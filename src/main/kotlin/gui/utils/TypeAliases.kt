package gui.utils

import problem.obj.Agent
import problem.obj.Path
import problem.obj.Point

typealias BasicSolution = Map<Agent, Path>
typealias MutableBasicSolution = MutableMap<Agent, Path>
typealias VertexConflict = Pair<Point, Int>
typealias EdgeConflict = Pair<Pair<Point, Point>, Int>
