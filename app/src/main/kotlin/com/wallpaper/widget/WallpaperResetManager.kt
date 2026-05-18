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
import java.io.File
import java.io.FileOutputStream

object WallpaperResetManager {

    private const val DEFAULT_WALLPAPER_FILE = "default_wallpaper.jpg"

    /** Save the given image URI as the default wallpaper. */
    fun saveDefaultWallpaper(context: Context, uri: Uri, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = WallpaperHelper.decodeBitmapForDevice(context, uri)
                val file = File(context.filesDir, DEFAULT_WALLPAPER_FILE)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                WallpaperPreferences.saveDefaultWallpaperPath(context, file.absolutePath)
                withContext(Dispatchers.Main) { callback(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { callback(false) }
            }
        }
    }

    /** Restore the previously saved default wallpaper. */
    fun resetToDefault(context: Context, callback: ((Boolean) -> Unit)? = null) {
        val path = WallpaperPreferences.getDefaultWallpaperPath(context)
        if (path == null) {
            callback?.invoke(false)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(path)
                if (!file.exists()) {
                    withContext(Dispatchers.Main) { callback?.invoke(false) }
                    return@launch
                }
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    ?: throw IllegalStateException("Failed to decode default wallpaper")
                val wallpaperManager = WallpaperManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val flags = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    wallpaperManager.setBitmap(bitmap, null, true, flags)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
                withContext(Dispatchers.Main) { callback?.invoke(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { callback?.invoke(false) }
            }
        }
    }

    fun getDefaultWallpaperBitmap(context: Context): Bitmap? {
        val path = WallpaperPreferences.getDefaultWallpaperPath(context) ?: return null
        val file = File(path)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }
}
