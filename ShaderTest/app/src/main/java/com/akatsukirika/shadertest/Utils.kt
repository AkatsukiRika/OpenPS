package com.akatsukirika.shadertest

import android.content.Context
import java.io.IOException
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

    fun getShaderCodeFromAssets(context: Context, fileName: String): String {
        val shaderCode = StringBuilder()
        try {
            context.assets.open(fileName).bufferedReader().use { reader ->
                reader.forEachLine {
                    shaderCode.append(it).append("\n")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return shaderCode.toString()
    }
}