package problem.obj

class Graph {
    private val adjacencyList: MutableMap<Point, MutableSet<Point>> = mutableMapOf()

    fun addVertex(point: Point) {
        adjacencyList.putIfAbsent(point, mutableSetOf())
    }

    fun addEdge(point1: Point, point2: Point) {
        adjacencyList[point1]?.add(point2)
        adjacencyList[point2]?.add(point1)
    }

    fun getNeighbors(point: Point): List<Point> {
        return adjacencyList[point]?.toList() ?: emptyList()
    }

    fun size() = adjacencyList.size
}
