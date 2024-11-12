package com.akatsukirika.openps.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.GalleryScreen
import com.akatsukirika.openps.databinding.ActivityGalleryBinding
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.utils.FrameRateObserver
import com.akatsukirika.openps.viewmodel.GalleryViewModel
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGalleryBinding
    private val viewModel: GalleryViewModel by viewModels()

    private val frameRateObserver by lazy {
        FrameRateObserver(object : FrameRateObserver.Callback {
            override fun onFrameRateChanged(fps: Double) {
                viewModel.uiFrameRate.value = fps
            }
        }, updateIntervalMillis = 100)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.gallery)
        }
        viewModel.init(
            this,
            selectImageCallback = {
                EditActivity.startMe(this, it)
            }
        )

        binding.tvFrameRate.visibility = if (SettingsStore.isDebugMode) View.VISIBLE else View.GONE
        if (SettingsStore.isDebugMode) {
            frameRateObserver.startObserve()
            lifecycleScope.launch {
                viewModel.uiFrameRate.collect {
                    binding.tvFrameRate.text = it.toInt().toString()
                }
            }
        }

        binding.composeView.setContent {
            GalleryScreen(viewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (SettingsStore.isDebugMode) {
            frameRateObserver.endObserve()
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

    companion object {
        fun startMe(activity: Activity) {
            val intent = Intent(activity, GalleryActivity::class.java)
            activity.startActivity(intent)
        }
    }
}