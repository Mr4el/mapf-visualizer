package gui.utils

import androidx.compose.ui.graphics.Color

object Utils {
    fun updateFps(
        frameStartTimeNanos: Long,
        frameCurrentTimeNanos: Long,
        onFpsUpdate: (Int) -> Unit,
        onFrameStartTimeNanosUpdate: (Long) -> Unit,
    ) {
        val timeElapsed = frameCurrentTimeNanos - frameStartTimeNanos
        val currentFrameFps = 1_000_000_000L / timeElapsed
        onFpsUpdate(currentFrameFps.toInt())
        onFrameStartTimeNanosUpdate(frameCurrentTimeNanos)
    }

    fun Long.formatAsElapsedTime(): String {
        val days = (this / (24 * 60 * 60 * 1000)) % 24
        val hours = (this / (60 * 60 * 1000)) % 24
        val minutes = (this / (60 * 1000)) % 60
        val seconds = (this / (1000)) % 60
        val milliseconds = this % 1000

        return when {
            days > 0 -> String.format("%2dd %2dh", days, hours)
            hours > 0 -> String.format("%2dh %2dm", hours, minutes)
            minutes > 0 -> String.format("%2dm %2ds", minutes, seconds)
            else -> String.format("%2ds %03dms", seconds, milliseconds)
        }
    }

    fun lightenColor(color: Int): Color {
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
