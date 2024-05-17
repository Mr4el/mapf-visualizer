package gui.utils

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
}
