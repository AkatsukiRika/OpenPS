package com.pixpark.gpupixel.model

data class PixelsResult(
    val data: ByteArray,
    val width: Int,
    val height: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PixelsResult

        if (!data.contentEquals(other.data)) return false
        if (width != other.width) return false
        return height == other.height
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}
