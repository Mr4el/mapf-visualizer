package problem

import problem.obj.Point

class Obstacle(
    x: Int,
    y: Int,
): Point(x, y) {
    companion object {
        fun Collection<Obstacle>.hasObstacleAt(x: Int, y: Int): Boolean {
            return this.any { it.isAt(x, y) }
        }

        fun Collection<Obstacle>.hasObstacleAt(point: Point): Boolean {
            return this.any { it.isAt(point) }
        }
    }
}
