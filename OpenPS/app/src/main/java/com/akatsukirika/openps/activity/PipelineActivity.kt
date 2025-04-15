package com.akatsukirika.openps.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.PipelineScreen
import com.akatsukirika.openps.databinding.ActivityPipelineBinding
import com.akatsukirika.openps.viewmodel.PipelineViewModel

class PipelineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPipelineBinding
    private val viewModel: PipelineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPipelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.visualize_pipeline)
        }

        binding.composeView.setContent {
            PipelineScreen(viewModel)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> {
                true
            }
        }
    }

    companion object {
        fun startMe(activity: Activity) {
            val intent = Intent(activity, PipelineActivity::class.java)
            activity.startActivity(intent)
        }
    }
}