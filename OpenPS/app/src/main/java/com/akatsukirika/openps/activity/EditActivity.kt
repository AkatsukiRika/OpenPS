package com.akatsukirika.openps.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.akatsukirika.openps.R
import com.akatsukirika.openps.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.image_edit)
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
        private const val EXTRA_KEY_IMAGE_URI = "image_uri"

        fun startMe(activity: Activity, imageUri: Uri) {
            val intent = Intent(activity, EditActivity::class.java).apply {
                putExtra(EXTRA_KEY_IMAGE_URI, imageUri)
            }
            activity.startActivity(intent)
        }
    }
}