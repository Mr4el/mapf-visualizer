package gui

import java.lang.Math.PI

object Constants {
    // Display config
    const val CELL_SIZE_PX = 50f

    // Agents
    const val TARGET_SIZE_RELATIVE_TO_CELL = 0.7f

    // Arrows
    const val ARROW_WIDTH_PX = 4f
    const val ARROW_RADIUS_PX = 15f
    const val ARROW_HEAD_LENGTH_PX = 15f
    const val ARROW_HEAD_ANGLE = PI / 6
    const val ARROW_SHORTEN_LENGTH_RELATIVE_TO_CELL = 0.45f

    const val TRANSITION_DURATION_MS = 100

    // Zoom config
    const val DEFAULT_SCALE = 1f
    const val MIN_ZOOM = 0.25f
    const val MAX_ZOOM = 3f
    const val SCROLL_LAMBDA = 0.2f
}