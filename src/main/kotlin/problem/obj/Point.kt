package problem.obj

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

open class Point(
    val x: Int,
    val y: Int,
) {
    operator fun component1(): Int = x
    operator fun component2(): Int = y

    val floatX = x.toFloat()
    val floatY = y.toFloat()
    val dpX = x.dp
    val dpY = y.dp

    fun toGridPoint(scaledCellSize: Dp, compensator: Dp = 0.dp) = Point(
        x = (scaledCellSize.value * x.toFloat() + compensator.value).toInt(),
        y = (scaledCellSize.value * y.toFloat() + compensator.value).toInt(),
    )

    open fun isAt(x: Int, y: Int): Boolean {
        return this.x == x && this.y == y
    }

    companion object {
        fun Point.equal(point: Point): Boolean {
            return this.x == point.x && this.y == point.y
        }

        fun Point.atOrAfter(x: Int, y: Int): Boolean {
            return this.y >= y || this.x >= x
        }
    }
}
