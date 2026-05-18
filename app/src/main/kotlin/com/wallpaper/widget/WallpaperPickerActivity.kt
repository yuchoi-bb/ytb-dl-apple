package com.wallpaper.widget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.wallpaper.widget.databinding.ActivityWallpaperPickerBinding

class WallpaperPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWallpaperPickerBinding
    private var selectedUri: Uri? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedUri = uri
                Glide.with(this).load(uri).centerCrop().into(binding.ivPreview)
                binding.btnApply.isEnabled = true
            }
        } else {
            finish()
        }
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchGallery()
        else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickImage.setOnClickListener { checkPermission() }
        binding.btnApply.isEnabled = false
        binding.btnApply.setOnClickListener { showTargetDialog() }
        binding.btnCancel.setOnClickListener { finish() }

        checkPermission()
    }

    private fun checkPermission() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED)
            launchGallery()
        else
            requestPermission.launch(perm)
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        pickImage.launch(intent)
    }

    private fun showTargetDialog() {
        val options = arrayOf(
            getString(R.string.home_screen),
            getString(R.string.lock_screen),
            getString(R.string.both_screens)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.select_target)
            .setItems(options) { _, which ->
                val target = when (which) {
                    0 -> WallpaperTarget.HOME
                    1 -> WallpaperTarget.LOCK
                    else -> WallpaperTarget.BOTH
                }
                applyWallpaper(target)
            }
            .show()
    }

    private fun applyWallpaper(target: WallpaperTarget) {
        val uri = selectedUri ?: return
        binding.btnApply.isEnabled = false
        WallpaperHelper.setWallpaper(this, uri, target) { success ->
            val msg = if (success) R.string.wallpaper_set_success else R.string.wallpaper_set_failed
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
