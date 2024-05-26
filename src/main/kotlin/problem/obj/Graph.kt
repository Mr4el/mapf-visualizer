package problem.obj

class Graph {
    private val adjacencyList: MutableMap<Point, MutableSet<Point>> = mutableMapOf()

    fun addVertex(point: Point) {
        adjacencyList.computeIfAbsent(point) { mutableSetOf() }
    }

    fun addEdge(point1: Point, point2: Point) {
        adjacencyList.computeIfAbsent(point1) { mutableSetOf() }.add(point2)
        adjacencyList.computeIfAbsent(point2) { mutableSetOf() }.add(point1)
    }

    fun getNeighbors(point: Point): Set<Point> {
        return adjacencyList[point] ?: emptySet()
    }

    fun size() = adjacencyList.size
}
