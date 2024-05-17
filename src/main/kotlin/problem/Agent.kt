package problem

import problem.obj.Path
import problem.obj.Point
import androidx.compose.ui.graphics.Color
import gui.style.CustomColors.DARK_GRAY
import gui.style.CustomColors.LIGHT_GRAY

class Agent(
    val name: String = "",
    val primaryColor: Color = DARK_GRAY,
    val secondaryColor: Color = LIGHT_GRAY,

    var startPosition: Point,
    var targetPosition: Point,
    var path: Path = Path(startPosition),

    var showPath: Boolean = true,

    x: Int = startPosition.x,
    y: Int = startPosition.y,
): Point(x, y) {
    override fun isAt(x: Int, y: Int): Boolean {
        return startPosition.isAt(x, y) || targetPosition.isAt(x, y)
    }

    fun clearPath() {
        path = Path(startPosition)
        showPath = true
    }

    companion object {
        fun Collection<Agent>.hasAgentAt(x: Int, y: Int): Boolean {
            return this.any { it.isAt(x, y) }
        }
    }
}
