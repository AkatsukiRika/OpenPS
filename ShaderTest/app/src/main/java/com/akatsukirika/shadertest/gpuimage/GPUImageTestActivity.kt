package com.akatsukirika.shadertest.gpuimage

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.akatsukirika.shadertest.R
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageTransformFilter

class GPUImageTestActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, GPUImageTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var gpuImageView: GPUImageView
    private val transformHelper by lazy {
        OpenGLTransformHelper()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpu_image_test)

        gpuImageView = findViewById(R.id.gpu_image_view)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test_gpu_image)
        gpuImageView.setImage(bitmap)
        gpuImageView.filter = GPUImageTransformFilter()

        transformHelper.setViewportSize(gpuImageView.width.toFloat(), gpuImageView.height.toFloat())
    }
}