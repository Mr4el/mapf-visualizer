package enums

import androidx.compose.ui.graphics.Color
import gui.style.CustomColors

enum class AgentColor(val primaryArgb: Color, val secondaryArgb: Color) {
    GRAY(primaryArgb = CustomColors.DARK_GRAY, secondaryArgb = CustomColors.LIGHT_GRAY),
    ORANGE(primaryArgb = CustomColors.ORANGE, secondaryArgb = CustomColors.LIGHT_ORANGE),
    GREEN(primaryArgb = CustomColors.GREEN, secondaryArgb = CustomColors.LIGHT_GREEN),
    RED(primaryArgb = CustomColors.RED, secondaryArgb = CustomColors.LIGHT_RED),
    PURPLE(primaryArgb = CustomColors.PURPLE, secondaryArgb = CustomColors.LIGHT_PURPLE),
    BLUE(primaryArgb = CustomColors.BLUE, secondaryArgb = CustomColors.LIGHT_BLUE);

    companion object {
        val ALL_DEFAULT_AGENT_COLORS = (AgentColor.values().toList() - GRAY).toSet()
    }
}