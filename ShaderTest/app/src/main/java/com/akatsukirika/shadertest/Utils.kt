package com.akatsukirika.shadertest

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object Utils {
    fun FloatArray.toNativeBuffer(): FloatBuffer = ByteBuffer
        .allocateDirect(this.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    fun ShortArray.toNativeBuffer(): ShortBuffer = ByteBuffer
        .allocateDirect(this.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
}