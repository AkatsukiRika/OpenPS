package com.akatsukirika.shadertest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.akatsukirika.shadertest.gpuimage.GPUImageTestActivity

class MainActivity : AppCompatActivity() {
    private lateinit var renderView: MyGLSurfaceView
    private lateinit var btnGpuImage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        renderView = findViewById(R.id.render_view)
        btnGpuImage = findViewById(R.id.btn_gpu_image)
        btnGpuImage.setOnClickListener {
            GPUImageTestActivity.start(this)
        }
    }

    override fun onPause() {
        super.onPause()
        renderView.onPause()
    }

    override fun onResume() {
        super.onResume()
        renderView.onResume()
    }
}