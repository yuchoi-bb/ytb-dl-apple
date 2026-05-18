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
                val bitmap = decodeBitmapForDevice(context, uri)
                applyBitmap(context, bitmap, target)
                withContext(Dispatchers.Main) { callback(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { callback(false) }
            }
        }
    }

    /** Decode and center-crop the image to exactly match device wallpaper dimensions. */
    fun decodeBitmapForDevice(context: Context, uri: Uri): Bitmap {
        val (targetW, targetH) = DeviceInfo.getDesiredWallpaperSize(context)

        // First pass: measure source dimensions
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }

        opts.inSampleSize = calculateSampleSize(opts.outWidth, opts.outHeight, targetW, targetH)
        opts.inJustDecodeBounds = false

        val sampled = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: throw IllegalStateException("Cannot open image")

        return centerCrop(sampled, targetW, targetH)
    }

    private fun applyBitmap(context: Context, bitmap: Bitmap, target: WallpaperTarget) {
        val wm = WallpaperManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val flags = when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }
            wm.setBitmap(bitmap, null, true, flags)
        } else {
            wm.setBitmap(bitmap)
        }
    }

    /** Center-crops `src` to exactly `targetW × targetH`. */
    private fun centerCrop(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        if (src.width == targetW && src.height == targetH) return src

        val srcRatio = src.width.toFloat() / src.height
        val dstRatio = targetW.toFloat() / targetH

        val (cropW, cropH) = if (srcRatio > dstRatio) {
            Pair((src.height * dstRatio).toInt(), src.height)
        } else {
            Pair(src.width, (src.width / dstRatio).toInt())
        }

        val x = (src.width - cropW) / 2
        val y = (src.height - cropH) / 2

        val cropped = Bitmap.createBitmap(src, x, y, cropW.coerceAtLeast(1), cropH.coerceAtLeast(1))
        return if (cropped.width == targetW && cropped.height == targetH) cropped
        else Bitmap.createScaledBitmap(cropped, targetW, targetH, true)
    }

    private fun calculateSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
        var sample = 1
        if (srcH > reqH || srcW > reqW) {
            val halfH = srcH / 2
            val halfW = srcW / 2
            while (halfH / sample >= reqH && halfW / sample >= reqW) sample *= 2
        }
        return sample
    }
}
