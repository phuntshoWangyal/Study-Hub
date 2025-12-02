package ca.unb.mobiledev.studyhub

object TimeUtils {

    fun formatDurationMs(totalMs: Long): String {
        val hours = (totalMs / 3_600_000).toInt()
        val minutes = ((totalMs % 3_600_000) / 60_000).toInt()
        val seconds = ((totalMs % 60_000) / 1000).toInt()

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    fun calculateDeltaHours(currentTotalMs: Double, lastSavedHours: Long): Double {
        val currentTotalHours = currentTotalMs.toDouble() / 3_600_000.0
        return currentTotalHours - lastSavedHours
    }
}
