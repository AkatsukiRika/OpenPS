package com.akatsukirika.openps.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.EditScreen
import com.akatsukirika.openps.compose.EditScreenCallback
import com.akatsukirika.openps.databinding.ActivityEditBinding
import com.akatsukirika.openps.interop.NativeLib
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.utils.BitmapUtils
import com.akatsukirika.openps.utils.LogUtils
import com.akatsukirika.openps.utils.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pixpark.gpupixel.GPUPixel
import com.pixpark.gpupixel.GPUPixel.GPUPixelLandmarkCallback
import com.pixpark.gpupixel.GPUPixelSourceImage
import com.pixpark.gpupixel.filter.BeautyFaceFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private var imageUri: Uri? = null

    private var sourceImage: GPUPixelSourceImage? = null
    private var beautyFaceFilter: BeautyFaceFilter? = null

    private var skinMaskBitmap: Bitmap? = null
    private var showFaceRect: Boolean = false

    // Face Rect
    private var faceRectLeft: Float = 0f
    private var faceRectTop: Float = 0f
    private var faceRectRight: Float = 0f
    private var faceRectBottom: Float = 0f
    private val faceRectExpandRatio: Float = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GPUPixel.setContext(this)

        imageUri = intent.getParcelableExtra(EXTRA_KEY_IMAGE_URI)
        loadImage()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.image_edit)
        }

        binding.surfaceView.setCallback { _, _, _ ->
            lifecycleScope.launch {
                delay(50)
                sourceImage?.proceed()
            }
        }
        binding.composeView.setContent {
            EditScreen(callback = object : EditScreenCallback {
                override fun onSetSmoothLevel(level: Float) {
                    beautyFaceFilter?.smoothLevel = level
                    sourceImage?.proceed()
                }

                override fun onSetWhiteLevel(level: Float) {
                    beautyFaceFilter?.whiteLevel = level
                    sourceImage?.proceed()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeLib.releaseBitmap()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!SettingsStore.isDebugMode) {
            return super.onCreateOptionsMenu(menu)
        }

        menuInflater.inflate(R.menu.menu_edit, menu)
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
                showFaceRect = !showFaceRect
                if (showFaceRect) {
                    initFaceRect()
                    item.setIcon(R.drawable.ic_visibility_off)
                } else {
                    binding.overlayView.visibility = View.GONE
                    item.setIcon(R.drawable.ic_visibility)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadImage() {
        imageUri?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = if (SettingsStore.photoSizeLimit != SettingsStore.PHOTO_SIZE_NO_LIMIT) {
                    Glide.with(this@EditActivity)
                        .asBitmap()
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(getSizeLimit())
                        .submit()
                        .get()
                } else {
                    Glide.with(this@EditActivity)
                        .asBitmap()
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .submit()
                        .get()
                }
                var result = NativeLib.loadBitmap(bitmap)
                LogUtils.d("loadImage result: $result")
                if (result != 0) {
                    ToastUtils.showToast(this@EditActivity, getString(R.string.msg_image_load_fail))
                    return@launch
                }
                result = NativeLib.runSkinModelInference(assets, "79999_iter.tflite")
                LogUtils.d("runSkinModelInference result: $result")
                if (result != 0) {
                    ToastUtils.showToast(this@EditActivity, getString(R.string.msg_image_process_fail))
                    return@launch
                }
                skinMaskBitmap = NativeLib.getSkinMaskBitmap()
                if (skinMaskBitmap == null) {
                    ToastUtils.showToast(this@EditActivity, getString(R.string.msg_image_process_fail))
                    return@launch
                }
                skinMaskBitmap?.let {
                    BitmapUtils.saveBitmapToFile(it, GPUPixel.getResource_path(), "skin_mask.png")
                    withContext(Dispatchers.Main) {
                        binding.debugImageView.setImageBitmap(it)
                    }
                }
                startImageFilter(bitmap)
            }
        }
    }

    private fun getSizeLimit() = when (SettingsStore.photoSizeLimit) {
        SettingsStore.PHOTO_SIZE_LIMIT_4K -> 4096
        SettingsStore.PHOTO_SIZE_LIMIT_2K -> 2048
        SettingsStore.PHOTO_SIZE_LIMIT_1K -> 1024
        else -> Int.MAX_VALUE
    }

    private fun startImageFilter(bitmap: Bitmap) {
        beautyFaceFilter = BeautyFaceFilter()
        sourceImage = GPUPixelSourceImage(bitmap)
        sourceImage?.setLandmarkCallback(object : GPUPixelLandmarkCallback {
            override fun onFaceLandmark(landmarks: FloatArray?) {}

            override fun onFaceLandmark(landmarks: FloatArray?, rect: FloatArray?) {
                if (rect != null && rect.size == 4) {
                    val rectLeft = rect[0]
                    val rectTop = rect[1]
                    val rectRight = rect[2]
                    val rectBottom = rect[3]
                    val rectWidth = abs(rectRight - rectLeft)
                    val rectHeight = abs(rectBottom - rectTop)
                    // 扩大人脸框区域
                    faceRectLeft = (rectLeft - rectWidth * faceRectExpandRatio).coerceAtLeast(0f)
                    faceRectTop = (rectTop - rectHeight * faceRectExpandRatio).coerceAtLeast(0f)
                    faceRectRight = (rectRight + rectWidth * faceRectExpandRatio).coerceAtMost(1f)
                    faceRectBottom = (rectBottom + rectHeight * faceRectExpandRatio).coerceAtMost(1f)
                }
            }
        })
        sourceImage?.addTarget(beautyFaceFilter)
        beautyFaceFilter?.addTarget(binding.surfaceView)
        sourceImage?.render()
        sourceImage?.proceed()
    }

    private fun initFaceRect() {
        binding.surfaceView.getInfo { viewWidth, viewHeight, scaledWidth, scaledHeight ->
            Log.d("xuanTest", "faceRectLeft: $faceRectLeft, faceRectTop: $faceRectTop, faceRectRight: $faceRectRight, faceRectBottom: $faceRectBottom")
            Log.d("xuanTest", "viewWidth: $viewWidth, viewHeight: $viewHeight, scaledWidth: $scaledWidth, scaledHeight: $scaledHeight")
            binding.overlayView.setData(viewWidth, viewHeight, scaledWidth, scaledHeight, RectF(faceRectLeft, faceRectTop, faceRectRight, faceRectBottom))
            runOnUiThread {
                binding.overlayView.visibility = View.VISIBLE
            }
        }
        sourceImage?.proceed()
    }

    companion object {
        private const val EXTRA_KEY_IMAGE_URI = "image_uri"

        fun startMe(activity: Activity, imageUri: Uri) {
            val intent = Intent(activity, EditActivity::class.java).apply {
                putExtra(EXTRA_KEY_IMAGE_URI, imageUri)
            }
            activity.startActivity(intent)
        }
    }
}