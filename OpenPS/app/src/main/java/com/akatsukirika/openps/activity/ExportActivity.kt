package com.akatsukirika.openps.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.R
import com.akatsukirika.openps.databinding.ActivityExportBinding
import com.akatsukirika.openps.utils.BitmapUtils
import com.akatsukirika.openps.utils.ToastUtils
import com.pixpark.gpupixel.model.PixelsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.image_save)
        }

        pixelsResultToBitmap()

        binding.llBack.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun pixelsResultToBitmap() {
        lifecycleScope.launch(Dispatchers.IO) {
            pixelsResult?.let {
                val resultBitmap = Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
                val byteArray = it.data
                val argbArray = IntArray(it.width * it.height)
                for (i in 0 until it.width * it.height) {
                    val r = byteArray[i * 4].toInt() and 0xFF
                    val g = byteArray[i * 4 + 1].toInt() and 0xFF
                    val b = byteArray[i * 4 + 2].toInt() and 0xFF
                    val a = byteArray[i * 4 + 3].toInt() and 0xFF
                    argbArray[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
                resultBitmap.setPixels(argbArray, 0, it.width, 0, 0, it.width, it.height)
                val fileName = generateFileName()
                val uri = BitmapUtils.saveBitmapToGallery(this@ExportActivity, resultBitmap, fileName)
                withContext(Dispatchers.Main) {
                    if (uri == null) {
                        ToastUtils.showToast(this@ExportActivity, getString(R.string.msg_image_save_fail))
                    }
                    binding.resultImage.setImageBitmap(resultBitmap)
                    binding.llBack.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun generateFileName() = "OpenPS_${System.currentTimeMillis() / 1000}.jpg"

    companion object {
        var pixelsResult: PixelsResult? = null
    }
}