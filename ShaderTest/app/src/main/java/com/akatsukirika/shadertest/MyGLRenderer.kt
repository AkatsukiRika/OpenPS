package com.akatsukirika.shadertest

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import com.akatsukirika.shadertest.Utils.toNativeBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var program = -1
    private var textureId = -1

    private var viewWidth = -1
    private var viewHeight = -1

    private var imageWidth = -1
    private var imageHeight = -1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1f, 1f, 1f, 1f)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, Utils.getShaderCodeFromAssets(context, "vertex_shader.glsl"))
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, Utils.getShaderCodeFromAssets(context, "saturation_fragment_shader.glsl"))

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        textureId = loadTexture(R.drawable.test)
        viewWidth = width
        viewHeight = height
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        val saturationLocation = GLES20.glGetUniformLocation(program, "saturation")
        GLES20.glUniform1f(saturationLocation, 1.0f)

        val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)
        val drawOrderBuffer = drawOrder.toNativeBuffer()
        drawOrderBuffer.put(drawOrder)
        drawOrderBuffer.rewind()

        val vertexArr = getVertices()
        val vertexBuffer = vertexArr.toNativeBuffer()
        vertexBuffer.put(vertexArr)
        vertexBuffer.rewind()

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)

        val textureHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(textureHandle)
        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawOrderBuffer)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun loadTexture(resourceId: Int): Int {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            val options = BitmapFactory.Options()
            options.inScaled = false

            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
            imageWidth = bitmap.width
            imageHeight = bitmap.height
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }
        return textureHandle[0]
    }

    /**
     * 根据View和图片的宽高，算出纹理顶点坐标
     * 计算方式是宽度占满，高度按图片比例垂直居中
     */
    private fun getVertices(): FloatArray {
        val ratioView = viewWidth.toFloat() / viewHeight
        val ratioImage = imageWidth.toFloat() / imageHeight
        val vertices = if (ratioView > ratioImage) {
            val height = viewHeight.toFloat()
            val width = height * ratioImage
            floatArrayOf(
                -width / viewWidth, 1.0f, 0.0f, 0.0f, 1.0f,
                -width / viewWidth, -1.0f, 0.0f, 0.0f, 0.0f,
                width / viewWidth, -1.0f, 0.0f, 1.0f, 0.0f,
                width / viewWidth, 1.0f, 0.0f, 1.0f, 1.0f
            )
        } else {
            val width = viewWidth.toFloat()
            val height = width / ratioImage
            floatArrayOf(
                -1.0f, height / viewHeight, 0.0f, 0.0f, 1.0f,
                -1.0f, -height / viewHeight, 0.0f, 0.0f, 0.0f,
                1.0f, -height / viewHeight, 0.0f, 1.0f, 0.0f,
                1.0f, height / viewHeight, 0.0f, 1.0f, 1.0f
            )
        }
        return vertices
    }
}