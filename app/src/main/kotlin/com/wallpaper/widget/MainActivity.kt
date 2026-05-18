package com.wallpaper.widget

import android.Manifest
import android.app.WallpaperManager
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
import com.wallpaper.widget.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedImageUri: Uri? = null

    // 이미지 선택 결과 처리
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                showPreview(uri)
            }
        }
    }

    // 권한 요청 결과 처리
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openGallery()
        else Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnSelectImage.setOnClickListener { checkPermissionAndOpenGallery() }
        binding.btnSetWallpaper.setOnClickListener { showWallpaperTargetDialog() }
        binding.btnSetWallpaper.isEnabled = false
    }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED ->
                openGallery()
            shouldShowRequestPermissionRationale(permission) -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.permission_needed)
                    .setMessage(R.string.permission_rationale)
                    .setPositiveButton(R.string.grant) { _, _ -> requestPermissionLauncher.launch(permission) }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun showPreview(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.ivPreview)

        binding.btnSetWallpaper.isEnabled = true
        binding.tvHint.text = getString(R.string.image_selected)
    }

    private fun showWallpaperTargetDialog() {
        val options = arrayOf(
            getString(R.string.home_screen),
            getString(R.string.lock_screen),
            getString(R.string.both_screens)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.select_target)
            .setItems(options) { _, which ->
                selectedImageUri?.let { uri ->
                    val target = when (which) {
                        0 -> WallpaperTarget.HOME
                        1 -> WallpaperTarget.LOCK
                        else -> WallpaperTarget.BOTH
                    }
                    setWallpaper(uri, target)
                }
            }
            .show()
    }

    private fun setWallpaper(uri: Uri, target: WallpaperTarget) {
        binding.btnSetWallpaper.isEnabled = false
        binding.tvStatus.text = getString(R.string.setting_wallpaper)

        WallpaperHelper.setWallpaper(this, uri, target) { success ->
            runOnUiThread {
                binding.btnSetWallpaper.isEnabled = true
                if (success) {
                    binding.tvStatus.text = getString(R.string.wallpaper_set_success)
                    Toast.makeText(this, R.string.wallpaper_set_success, Toast.LENGTH_SHORT).show()
                } else {
                    binding.tvStatus.text = getString(R.string.wallpaper_set_failed)
                    Toast.makeText(this, R.string.wallpaper_set_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
