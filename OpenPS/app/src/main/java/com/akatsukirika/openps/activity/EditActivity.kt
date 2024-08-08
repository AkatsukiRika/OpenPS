package com.akatsukirika.openps.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.EditScreen
import com.akatsukirika.openps.compose.EditScreenCallback
import com.akatsukirika.openps.compose.STATUS_ERROR
import com.akatsukirika.openps.compose.STATUS_IDLE
import com.akatsukirika.openps.compose.STATUS_LOADING
import com.akatsukirika.openps.compose.STATUS_SUCCESS
import com.akatsukirika.openps.databinding.ActivityEditBinding
import com.akatsukirika.openps.interop.NativeLib
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.utils.BitmapUtils
import com.akatsukirika.openps.utils.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pixpark.gpupixel.GPUPixel
import com.pixpark.gpupixel.OpenPSHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private lateinit var helper: OpenPSHelper
    private var imageUri: Uri? = null

    private var skinMaskBitmap: Bitmap? = null
    private var showFaceRect: Boolean = true

    private val loadStatus = MutableStateFlow(STATUS_IDLE)

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

        helper = OpenPSHelper(binding.surfaceView)

        imageUri = intent.getParcelableExtra(EXTRA_KEY_IMAGE_URI)
        loadImage()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.image_edit)
        }

        binding.composeView.setContent {
            val status = loadStatus.collectAsState(initial = STATUS_IDLE).value

            EditScreen(callback = object : EditScreenCallback {
                override fun onSetSmoothLevel(level: Float) {
                    helper.setSmoothLevel(level)
                }

                override fun onSetWhiteLevel(level: Float) {
                    helper.setWhiteLevel(level)
                }

                override fun onSetLipstickLevel(level: Float) {
                    helper.setLipstickLevel(level)
                }
            }, loadStatus = status)
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
        helper.destroy()
        NativeLib.releaseBitmap()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        menu?.findItem(R.id.show_skin_mask)?.setVisible(SettingsStore.isDebugMode)
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
                if (loadStatus.value == STATUS_LOADING) {
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
        lifecycleScope.launch(Dispatchers.Main) {
            // 0. 使用最基本的渲染管线，先让图片显示到屏幕上（加载状态开始，不得操作效果滑杆）
            loadStatus.emit(STATUS_LOADING)
            helper.initWithImage(bitmap)
            helper.buildBasicRenderPipeline()
            helper.requestRender()

            // 1. 从Native层获取VNN人脸识别的结果
            val landmarkResult = helper.getLandmark()
            val rect = landmarkResult.rect
            if (rect != null && rect.size == 4) {
                val rectLeft = rect[0]
                val rectTop = rect[1]
                val rectRight = rect[2]
                val rectBottom = rect[3]
                val rectWidth = abs(rectRight - rectLeft)
                val rectHeight = abs(rectBottom - rectTop)

                // 2. 适当扩大人脸框的区域
                faceRectLeft = (rectLeft - rectWidth * faceRectExpandRatio).coerceAtLeast(0f)
                faceRectTop = (rectTop - rectHeight * faceRectExpandRatio).coerceAtLeast(0f)
                faceRectRight = (rectRight + rectWidth * faceRectExpandRatio).coerceAtMost(1f)
                faceRectBottom = (rectBottom + rectHeight * faceRectExpandRatio).coerceAtMost(1f)

                // 3. 根据原图缩放比例展示人脸框
                val renderViewInfo = helper.getRenderViewInfo()
                renderViewInfo?.let { info ->
                    binding.overlayView.setData(
                        info.viewWidth,
                        info.viewHeight,
                        info.scaledWidth,
                        info.scaledHeight,
                        RectF(faceRectLeft, faceRectTop, faceRectRight, faceRectBottom)
                    )
                    binding.overlayView.visibility = View.VISIBLE
                }

                // 4. 使用深度学习模型进行皮肤分割
                withContext(Dispatchers.IO) {
                    val croppedBitmap = BitmapUtils.cropBitmap(bitmap, faceRectLeft, faceRectTop, faceRectRight, faceRectBottom)
                    var result = NativeLib.loadBitmap(croppedBitmap)

                    if (result != 0) {
                        // 加载失败
                        loadStatus.emit(STATUS_ERROR)
                        ToastUtils.showToast(this@EditActivity, getString(R.string.msg_image_load_fail))
                        return@withContext
                    }

                    result = NativeLib.runSkinModelInference(assets, "79999_iter.tflite")
                    if (result != 0) {
                        // 加载失败
                        loadStatus.emit(STATUS_ERROR)
                        ToastUtils.showToast(this@EditActivity, getString(R.string.msg_image_process_fail))
                        return@withContext
                    }

                    skinMaskBitmap = NativeLib.getSkinMaskBitmap()
                    if (skinMaskBitmap == null) {
                        // 加载失败
                        loadStatus.emit(STATUS_ERROR)
                        ToastUtils.showToast(this@EditActivity, getString(R.string.msg_image_process_fail))
                        return@withContext
                    }

                    // 5. 把皮肤分割结果保存到资源目录
                    skinMaskBitmap = BitmapUtils.mergeBitmap(bitmap, skinMaskBitmap!!, faceRectLeft, faceRectTop, faceRectRight, faceRectBottom)
                    skinMaskBitmap?.let {
                        BitmapUtils.saveBitmapToFile(it, GPUPixel.getResource_path(), "skin_mask.png")

                        withContext(Dispatchers.Main) {
                            binding.debugImageView.setImageBitmap(it)

                            // 6. 重新搭建一套带美颜滤镜的渲染管线（加载成功，可以操作效果滑杆）
                            helper.buildRealRenderPipeline()
                            helper.requestRender()
                            loadStatus.emit(STATUS_SUCCESS)
                        }
                    }
                }
            }
        }
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