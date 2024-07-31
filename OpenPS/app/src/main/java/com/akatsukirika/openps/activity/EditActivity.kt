package com.akatsukirika.openps.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
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
import com.pixpark.gpupixel.GPUPixelSourceImage
import com.pixpark.gpupixel.filter.BeautyFaceFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private var imageUri: Uri? = null

    private var sourceImage: GPUPixelSourceImage? = null
    private var beautyFaceFilter: BeautyFaceFilter? = null

    private var skinMaskBitmap: Bitmap? = null

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

        menuInflater.inflate(R.menu.menu_debug, menu)
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
                    skinMaskBitmap?.let { bitmap ->
                        binding.debugImageView.visibility = View.VISIBLE
                        binding.debugImageView.setImageBitmap(bitmap)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadImage() {
        imageUri?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = Glide.with(this@EditActivity)
                    .asBitmap()
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .submit()
                    .get()
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
                }
                startImageFilter(bitmap)
            }
        }
    }

    private fun startImageFilter(bitmap: Bitmap) {
        beautyFaceFilter = BeautyFaceFilter()
        sourceImage = GPUPixelSourceImage(bitmap)
        sourceImage?.setLandmarkCallback { landmarks ->
            landmarks.forEachIndexed { index, fl ->
                Log.d("xuanTest", "landmark $index: $fl")
            }
        }
        sourceImage?.addTarget(beautyFaceFilter)
        beautyFaceFilter?.addTarget(binding.surfaceView)
        sourceImage?.render()
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