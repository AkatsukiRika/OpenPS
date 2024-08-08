package com.pixpark.gpupixel.model

data class LandmarkResult(
    val landmarks: FloatArray?,
    val rect: FloatArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LandmarkResult

        if (landmarks != null) {
            if (other.landmarks == null) return false
            if (!landmarks.contentEquals(other.landmarks)) return false
        } else if (other.landmarks != null) return false
        if (rect != null) {
            if (other.rect == null) return false
            if (!rect.contentEquals(other.rect)) return false
        } else if (other.rect != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = landmarks?.contentHashCode() ?: 0
        result = 31 * result + (rect?.contentHashCode() ?: 0)
        return result
    }
}