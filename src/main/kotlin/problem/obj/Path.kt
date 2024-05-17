package problem.obj

import problem.obj.Point.Companion.equal

data class Path(
    var steps: List<Point> = emptyList()
) {
    constructor(vararg points: Point): this(points.toList())

    val length = steps.size

    fun timeStepPosition(timeStep: Int): Point {
        return steps[(timeStep).coerceIn(0, steps.size - 1)]
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

    fun takeNextCellOrNull(currentTimeStep: Int): Point? {
        for (i in currentTimeStep until steps.size) {
            val currentStep = timeStepPosition(i)
            val nextStep = timeStepPosition(i + 1)
            if (!currentStep.equal(nextStep)) {
                return nextStep
            }
        }
        return null
    }
}
