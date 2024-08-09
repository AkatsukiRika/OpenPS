package com.akatsukirika.shadertest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    private lateinit var renderView: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        renderView = findViewById(R.id.render_view)
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