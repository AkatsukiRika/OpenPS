package com.akatsukirika.openps.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.EditScreen
import com.akatsukirika.openps.compose.MODULE_ELIMINATE_PEN
import com.akatsukirika.openps.compose.MODULE_NONE
import com.akatsukirika.openps.compose.STATUS_LOADING
import com.akatsukirika.openps.compose.STATUS_SUCCESS
import com.akatsukirika.openps.databinding.ActivityEditBinding
import com.akatsukirika.openps.fragment.EliminatePenFragment
import com.akatsukirika.openps.interop.NativeLib
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.utils.FrameRateObserver
import com.akatsukirika.openps.viewmodel.EditViewModel
import com.akatsukirika.openps.viewmodel.EliminateViewModel
import com.pixpark.gpupixel.model.RenderViewInfo
import com.pixpark.gpupixel.view.OpenPSRenderView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private var imageUri: Uri? = null

    private var showFaceRect: Boolean = false

    private val viewModel: EditViewModel by viewModels()

    private val eliminateViewModel: EliminateViewModel by viewModels()

    private var eliminatePenFragment: EliminatePenFragment? = null

    // 原图->屏幕内居中适配的矩阵
    private val baseImageMatrix = Matrix()

    private val startExportForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    private val frameRateObserver by lazy {
        FrameRateObserver(object : FrameRateObserver.Callback {
            override fun onFrameRateChanged(fps: Double) {
                viewModel.uiFrameRate.value = fps
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.surfaceView.setCallback(object : OpenPSRenderView.Callback {
            override fun onMatrixChanged(matrix: Matrix) {
                binding.transformLayout.setTransform(matrix)
            }

            override fun onImageMatrixChanged(matrix: Matrix) {
                eliminatePenFragment?.onMatrixChange(matrix)
            }

            override fun onGLMatrixChanged(glMatrix: FloatArray) {
                viewModel.helper?.updateMVPMatrix(glMatrix)
            }

            override fun onFrameRateChanged(fps: Double) {
                viewModel.glFrameRate.value = fps
            }
        })
        viewModel.init(
            renderView = binding.surfaceView,
            callback = object : EditViewModel.Callback {
                override fun showOverlayView(info: RenderViewInfo, faceRectF: RectF) {
                    binding.overlayView.setData(
                        info.viewWidth,
                        info.viewHeight,
                        info.scaledWidth,
                        info.scaledHeight,
                        faceRectF
                    )
                    binding.surfaceView.transformHelper.setViewportSize(info.viewWidth, info.viewHeight)
                    initBaseMatrix(info)
                }

                override fun setDebugImage(bitmap: Bitmap) {
                    binding.debugImageView.setImageBitmap(bitmap)
                }

                override fun onRenderViewInfoReady(info: RenderViewInfo) {
                    binding.surfaceView.transformHelper.setViewportSize(info.viewWidth, info.viewHeight)
                    initBaseMatrix(info)
                }
            }
        )

        imageUri = intent.getParcelableExtra(EXTRA_KEY_IMAGE_URI)
        imageUri?.let {
            viewModel.loadImage(this, it)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.image_edit)
        }

        lifecycleScope.launch {
            launch {
                viewModel.showFrameRate.collect {
                    if (it) {
                        frameRateObserver.startObserve()
                    } else {
                        frameRateObserver.endObserve()
                    }
                }
            }

            launch {
                viewModel.uiFrameRate.combine(viewModel.glFrameRate) { uiFps, glFps ->
                    "UI: ${uiFps.toInt()}\nGL: ${glFps.toInt()}"
                }.collect {
                    binding.tvFrameRate.text = it
                }
            }

            launch {
                viewModel.selectedModule.collect {
                    if (it == MODULE_ELIMINATE_PEN) {
                        createEliminatePenFragment()
                    } else {
                        removeEliminatePenFragment()
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback {
            if (viewModel.selectedModule.value == MODULE_NONE) {
                finish()
            } else {
                viewModel.selectedModule.value = MODULE_NONE
            }
        }

        binding.composeView.setContent {
            EditScreen(viewModel, eliminateViewModel)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.surfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.surfaceView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
        NativeLib.releaseBitmap()
        frameRateObserver.endObserve()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        menu?.findItem(R.id.show_skin_mask)?.setVisible(SettingsStore.isDebugMode)
        menu?.findItem(R.id.show_frame_rate)?.setVisible(SettingsStore.isDebugMode)
        menu?.findItem(R.id.show_matrix_info)?.setVisible(SettingsStore.isDebugMode)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.show_skin_mask -> {
                if (binding.debugImageView.visibility == View.VISIBLE) {
                    binding.debugImageView.visibility = View.GONE
                } else {
                    binding.debugImageView.visibility = View.VISIBLE
                }
                true
            }
            R.id.show_face_rect -> {
                if (viewModel.loadStatus.value == STATUS_LOADING) {
                    return true
                }
                showFaceRect = !showFaceRect
                if (showFaceRect) {
                    binding.overlayView.visibility = View.VISIBLE
                    item.setIcon(R.drawable.ic_visibility_off)
                } else {
                    binding.overlayView.visibility = View.GONE
                    item.setIcon(R.drawable.ic_visibility)
                }
                true
            }
            R.id.show_frame_rate -> {
                viewModel.showFrameRate.update { !it }
                binding.tvFrameRate.visibility = if (viewModel.showFrameRate.value) View.VISIBLE else View.GONE
                true
            }
            R.id.show_matrix_info -> {
                viewModel.showMatrixInfo.update { !it }
                binding.transformLayout.setDebug(viewModel.showMatrixInfo.value)
                true
            }
            R.id.save_to_gallery -> {
                saveToGallery()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveToGallery() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.updateLoadStatus(STATUS_LOADING)
            ExportActivity.pixelsResult = viewModel.helper?.getResultPixels()
            viewModel.updateLoadStatus(STATUS_SUCCESS)
            withContext(Dispatchers.Main) {
                startExportForResult.launch(Intent(this@EditActivity, ExportActivity::class.java))
            }
        }
    }

    private fun createEliminatePenFragment() {
        eliminateViewModel.originalBitmap = viewModel.originalBitmap
        eliminateViewModel.matrix.value = binding.surfaceView.getImageMatrix()
        eliminatePenFragment = EliminatePenFragment(eliminateViewModel, binding.surfaceView)
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_eliminate_pen, eliminatePenFragment!!)
            .commit()
    }

    private fun removeEliminatePenFragment() {
        eliminatePenFragment?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    private fun initBaseMatrix(renderViewInfo: RenderViewInfo) {
        val imageWidth = viewModel.originalBitmap?.width ?: return
        val imageHeight = viewModel.originalBitmap?.height ?: return
        val scaledWidth = renderViewInfo.scaledWidth * renderViewInfo.viewWidth
        val scaledHeight = renderViewInfo.scaledHeight * renderViewInfo.viewHeight
        val baseScaleX = scaledWidth / imageWidth
        val baseScaleY = scaledHeight / imageHeight
        val translateY = (renderViewInfo.viewHeight - scaledHeight) / 2
        Log.d(TAG, "baseScaleX: $baseScaleX, baseScaleY: $baseScaleY, translateY: $translateY")
        baseImageMatrix.reset()
        baseImageMatrix.postScale(baseScaleX, baseScaleY)
        baseImageMatrix.postTranslate(0f, translateY)
        binding.surfaceView.initImageMatrix(baseImageMatrix)
    }

    companion object {
        const val TAG = "EditActivity"
        private const val EXTRA_KEY_IMAGE_URI = "image_uri"

        fun startMe(activity: Activity, imageUri: Uri) {
            val intent = Intent(activity, EditActivity::class.java).apply {
                putExtra(EXTRA_KEY_IMAGE_URI, imageUri)
            }
            activity.startActivity(intent)
        }
    }
}