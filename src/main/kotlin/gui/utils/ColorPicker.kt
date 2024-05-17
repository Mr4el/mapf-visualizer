package gui.utils

import androidx.compose.ui.graphics.Color
import gui.enums.AgentColor
import gui.enums.AgentColor.Companion.ALL_DEFAULT_AGENT_COLORS
import kotlin.random.Random

class ColorPicker(
    private var predefinedColors: MutableSet<AgentColor> = ALL_DEFAULT_AGENT_COLORS.toMutableSet(),
    private var usedColors: MutableSet<Color> = mutableSetOf(),
) {
    constructor(vararg usedColor: AgentColor): this() {
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
        ALL_DEFAULT_AGENT_COLORS.firstOrNull{ it.primaryArgb == color || it.secondaryArgb == color }?.let {
            predefinedColors.add(it)
        }
        usedColors.remove(color)
    }

    fun reset() {
        predefinedColors.clear()
        usedColors.clear()
        predefinedColors.addAll(ALL_DEFAULT_AGENT_COLORS)
    }

    private fun setUsedColor(color: Color) {
        usedColors.add(color)
        predefinedColors.removeIf{ it.primaryArgb == color || it.secondaryArgb == color }
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

    private fun lightenColor(color: Int): Color {
        val alpha = color shr 24 and 0xFF
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF

        val factor = 0.2

        val newRed = (red + (255 - red) * factor).toInt().coerceIn(0, 255)
        val newGreen = (green + (255 - green) * factor).toInt().coerceIn(0, 255)
        val newBlue = (blue + (255 - blue) * factor).toInt().coerceIn(0, 255)

        return Color(alpha shl 24 or (newRed shl 16) or (newGreen shl 8) or newBlue)
    }
}
