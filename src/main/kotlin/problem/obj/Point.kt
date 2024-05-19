package problem.obj

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

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

    fun isInBoundaries(x: Int, y: Int): Boolean {
        return this.x in 0 until x && this.y in 0 until y
    }

    companion object {
        fun Point.equal(point: Point): Boolean {
            return this.x == point.x && this.y == point.y
        }

        fun Point.atOrAfter(x: Int, y: Int): Boolean {
            return this.y >= y || this.x >= x
        }

        // Manhattan distance
        fun Point.distanceTo(point: Point): Int {
            val dx = abs(this.x - point.x)
            val dy = abs(this.y - point.y)
            return dx + dy
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (floatX != other.floatX) return false
        if (floatY != other.floatY) return false
        if (dpX != other.dpX) return false
        if (dpY != other.dpY) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + floatX.hashCode()
        result = 31 * result + floatY.hashCode()
        result = 31 * result + dpX.hashCode()
        result = 31 * result + dpY.hashCode()
        return result
    }
}