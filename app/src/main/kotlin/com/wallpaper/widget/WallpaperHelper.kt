package com.wallpaper.widget

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class WallpaperTarget { HOME, LOCK, BOTH }

object WallpaperHelper {

    fun setWallpaper(
        context: Context,
        uri: Uri,
        target: WallpaperTarget,
        callback: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = decodeSampledBitmap(context, uri)
                val wallpaperManager = WallpaperManager.getInstance(context)

                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                        val flags = when (target) {
                            WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                            WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                            WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        }
                        wallpaperManager.setBitmap(bitmap, null, true, flags)
                    }
                    else -> wallpaperManager.setBitmap(bitmap)
                }
                withContext(Dispatchers.Main) { callback(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { callback(false) }
            }
        }
    }

    private fun decodeSampledBitmap(context: Context, uri: Uri): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        val displayMetrics = context.resources.displayMetrics
        val reqWidth = displayMetrics.widthPixels
        val reqHeight = displayMetrics.heightPixels

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalStateException("Cannot open image stream")
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
