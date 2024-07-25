package com.akatsukirika.openps.activity

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.akatsukirika.openps.R
import com.akatsukirika.openps.databinding.ActivityMainBinding
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.utils.PermissionUtils
import com.pixpark.gpupixel.GPUPixel

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

        GPUPixel.setContext(this)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val popupMenu = PopupMenu(this, findViewById(R.id.action_settings))
                popupMenu.menuInflater.inflate(R.menu.menu_settings, popupMenu.menu)
                popupMenu.menu.findItem(R.id.debug_mode).isChecked = SettingsStore.isDebugMode
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.debug_mode -> {
                            SettingsStore.isDebugMode = !SettingsStore.isDebugMode
                            it.isChecked = SettingsStore.isDebugMode
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
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