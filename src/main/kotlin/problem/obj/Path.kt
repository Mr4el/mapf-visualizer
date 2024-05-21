package problem.obj

import problem.obj.Point.Companion.equal

data class Path(
    var steps: List<Point> = emptyList()
) {
    constructor(vararg points: Point) : this(points.toList())

    fun length() = steps.size

    fun timeStepPosition(timeStep: Int): Point {
        return steps[(timeStep).coerceIn(0, steps.size - 1)]
    }

    fun resetPath(point: Point) {
        steps = listOf(point)
    }

    fun takeNextCell(currentTimeStep: Int): Pair<Point, Int> {
        for (i in currentTimeStep until steps.size) {
            val currentStep = timeStepPosition(i)
            val nextStep = timeStepPosition(i + 1)
            if (!currentStep.equal(nextStep)) {
                return nextStep to i + 1
            }
        }
        return timeStepPosition(currentTimeStep) to currentTimeStep
    }

    fun takeNextCellOrNull(currentTimeStep: Int): Pair<Point?, Int> {
        for (i in currentTimeStep until steps.size) {
            val currentStep = timeStepPosition(i)
            val nextStep = timeStepPosition(i + 1)
            if (!currentStep.equal(nextStep)) {
                return nextStep to i + 1
            }
        }
        return null to 0
    }

    companion object {
        fun Collection<Path>.getSumOfCosts(): Int {
            return this.sumOf { it.length() - 1 }
        }

        fun Collection<Path>.getMakespan(): Int {
            return this.maxOf { it.length() - 1 }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Path) return false

        if (steps != other.steps) return false

        return true
    }

    override fun hashCode(): Int {
        return steps.hashCode()
    }
}
