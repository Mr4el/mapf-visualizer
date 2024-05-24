package gui.utils

import androidx.compose.ui.graphics.Color
import enums.AgentColor
import enums.AgentColor.Companion.ALL_DEFAULT_AGENT_COLORS
import gui.utils.Utils.lightenColor
import kotlin.random.Random

class ColorPicker(
    private var predefinedColors: MutableSet<AgentColor> = ALL_DEFAULT_AGENT_COLORS.toMutableSet(),
    private var usedColors: MutableSet<Color> = mutableSetOf(),
) {
    constructor(vararg usedColor: AgentColor) : this() {
        usedColor.forEach {
            setUsedColor(it.primaryArgb)
            setUsedColor(it.secondaryArgb)
        }
    }

    fun getNextColor(): Pair<Color, Color> {
        val usedColors = if (predefinedColors.isNotEmpty()) {
            val color = predefinedColors.minBy { it.ordinal }
            color.primaryArgb to color.secondaryArgb
        } else {
            generateRandomAgentColor()
        }

        setUsedColor(usedColors.first)
        setUsedColor(usedColors.second)

        return usedColors
    }

    fun freeColor(color: Color) {
        ALL_DEFAULT_AGENT_COLORS.firstOrNull {
            it.primaryArgb == color || it.secondaryArgb == color
        }?.let {
            predefinedColors.add(it)
        }
        usedColors.remove(color)
    }

    private fun setUsedColor(color: Color) {
        usedColors.add(color)
        predefinedColors.removeIf {
            it.primaryArgb == color || it.secondaryArgb == color
        }
    }

    private fun generateRandomAgentColor(): Pair<Color, Color> {
        val colorCode = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

        val randomPrimaryColor = Color(colorCode)
        val secondaryColor = lightenColor(colorCode)

        return if (usedColors.contains(randomPrimaryColor) || usedColors.contains(secondaryColor)) {
            generateRandomAgentColor()
        } else {
            randomPrimaryColor to secondaryColor
        }
    }
}
