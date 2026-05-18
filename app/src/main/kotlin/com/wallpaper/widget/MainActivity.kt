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
import com.wallpaper.widget.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedImageUri: Uri? = null

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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openGallery()
        else Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        showDeviceInfo()
        ScreenWatchService.start(this)
    }

    private fun setupUI() {
        binding.btnSelectImage.setOnClickListener { checkPermissionAndOpenGallery() }
        binding.btnSetWallpaper.setOnClickListener { showWallpaperTargetDialog() }
        binding.btnSetDefault.setOnClickListener { showSetDefaultDialog() }
        binding.btnSetWallpaper.isEnabled = false
        binding.btnSetDefault.isEnabled = false
        updateDefaultWallpaperPreview()
        updateAutoResetSwitch()
        binding.switchAutoReset.setOnCheckedChangeListener { _, checked ->
            WallpaperPreferences.setAutoResetEnabled(this, checked)
            if (checked) ScreenWatchService.start(this) else ScreenWatchService.stop(this)
        }
    }

    private fun showDeviceInfo() {
        val info = DeviceInfo.getScreenInfo(this)
        val (desiredW, desiredH) = DeviceInfo.getDesiredWallpaperSize(this)
        binding.tvDeviceModel.text = info.displayName
        binding.tvDeviceResolution.text = getString(
            R.string.device_resolution_fmt,
            info.resolution,
            info.densityLabel,
            info.densityDpi,
            desiredW,
            desiredH
        )
    }

    private fun updateDefaultWallpaperPreview() {
        val bmp = WallpaperResetManager.getDefaultWallpaperBitmap(this)
        if (bmp != null) {
            binding.ivDefaultPreview.setImageBitmap(bmp)
            binding.tvDefaultStatus.text = getString(R.string.default_set)
        } else {
            binding.ivDefaultPreview.setImageDrawable(null)
            binding.tvDefaultStatus.text = getString(R.string.default_not_set)
        }
    }

    private fun updateAutoResetSwitch() {
        binding.switchAutoReset.isChecked = WallpaperPreferences.isAutoResetEnabled(this)
    }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED ->
                openGallery()
            shouldShowRequestPermissionRationale(permission) ->
                AlertDialog.Builder(this)
                    .setTitle(R.string.permission_needed)
                    .setMessage(R.string.permission_rationale)
                    .setPositiveButton(R.string.grant) { _, _ -> requestPermissionLauncher.launch(permission) }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
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
        Glide.with(this).load(uri).centerCrop().into(binding.ivPreview)
        binding.btnSetWallpaper.isEnabled = true
        binding.btnSetDefault.isEnabled = true
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
                val target = when (which) {
                    0 -> WallpaperTarget.HOME
                    1 -> WallpaperTarget.LOCK
                    else -> WallpaperTarget.BOTH
                }
                selectedImageUri?.let { setWallpaper(it, target) }
            }
            .show()
    }

    private fun showSetDefaultDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.set_default_title)
            .setMessage(R.string.set_default_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                selectedImageUri?.let { uri ->
                    WallpaperResetManager.saveDefaultWallpaper(this, uri) { success ->
                        val msg = if (success) R.string.default_saved else R.string.wallpaper_set_failed
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        if (success) updateDefaultWallpaperPreview()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setWallpaper(uri: Uri, target: WallpaperTarget) {
        binding.btnSetWallpaper.isEnabled = false
        binding.tvStatus.text = getString(R.string.setting_wallpaper)
        WallpaperHelper.setWallpaper(this, uri, target) { success ->
            binding.btnSetWallpaper.isEnabled = true
            val msgRes = if (success) R.string.wallpaper_set_success else R.string.wallpaper_set_failed
            binding.tvStatus.text = getString(msgRes)
            Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show()
        }
    }
}
