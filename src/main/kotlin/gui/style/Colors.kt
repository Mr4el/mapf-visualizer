package gui.style

import androidx.compose.ui.graphics.Color

object CustomColors {
    // Common
    val WHITE = Color.White
    val BLACK = Color.Black

    // Material theme
    val PRIMARY = Color(0xff715189)
    val PRIMARY_VARIANT = Color(0xfff2daff)
    val SECONDARY = Color(0xff675a6e)
    val SECONDARY_VARIANT = Color(0xffeeddf5)
    val BACKGROUND = Color(0xFFF7FBF2)
    val SURFACE = Color(0xfffff7fd)
    val ERROR = Color(0xffba1a1a)
    val ON_PRIMARY = WHITE
    val ON_SECONDARY = BLACK
    val ON_BACKGROUND = Color(0xFF181D18)
    val ON_SURFACE = Color(0xff1e1a20)
    val ON_ERROR = WHITE

    // Agent colors
    val ORANGE = Color(0xffffcc99)
    val LIGHT_ORANGE = Color(0xffffe5cc)

    val GREEN = Color(0xffcdeb8b)
    val LIGHT_GREEN = Color(0xffe6f5c5)

    val RED = Color(0xffffcccc)
    val LIGHT_RED = Color(0xffffe5e5)

    val PURPLE = Color(0xffe1d5e7)
    val LIGHT_PURPLE = Color(0xffe7ddeb)

    val BLUE = Color(0xffcce5ff)
    val LIGHT_BLUE = Color(0xffd6eaff)

    // Grid colors
    val DARKER_GRAY = Color(0xff232526)
    val DARK_GRAY = Color(0xff3b4045)
    val LIGHTER_GRAY = Color(0xffe3e1e1)
    val LIGHT_GRAY = Color.LightGray

    val BACKGROUND_COLOR = SURFACE
    val DARK_BACKGROUND_COLOR = LIGHT_GRAY

    // Gui colors
    val ACTIVE_BUTTON_BACKGROUND = PRIMARY
    val ACTIVE_BUTTON_TEXT = WHITE
    val DISABLED_BUTTON_BACKGROUND = SURFACE
    val DISABLED_BUTTON_TEXT = BLACK
    val CHECKED_THUMB_COLOR = ACTIVE_BUTTON_BACKGROUND
}
