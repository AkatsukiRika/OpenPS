package com.akatsukirika.openps.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.GalleryScreen
import com.akatsukirika.openps.databinding.ActivityGalleryBinding
import com.akatsukirika.openps.viewmodel.GalleryViewModel

class GalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGalleryBinding
    private val viewModel: GalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.gallery)
        }
        viewModel.init(this)

        binding.composeView.setContent {
            GalleryScreen(viewModel)
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