package com.akatsukirika.openps.activity

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.akatsukirika.openps.databinding.ActivityMainBinding
import com.akatsukirika.openps.utils.PermissionUtils

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var binding: ActivityMainBinding
    private var selectImageLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                onImageSelected(it)
            }
        }

        binding.llSelectImage.setOnClickListener {
            if (PermissionUtils.checkAndRequestGalleryPermission(this, PERMISSION_REQUEST_CODE)) {
                selectImageLauncher?.launch("image/*")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageLauncher?.launch("image/*")
            }
        }
    }

    private fun onImageSelected(uri: Uri) {
        EditActivity.startMe(this, uri)
    }
}