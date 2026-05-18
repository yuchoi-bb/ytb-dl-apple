package com.wallpaper.widget

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

data class ScreenInfo(
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val manufacturer: String = Build.MANUFACTURER.replaceFirstChar { it.uppercaseChar() },
    val model: String = Build.MODEL
) {
    val displayName: String get() = "$manufacturer $model"
    val resolution: String get() = "${widthPx}×${heightPx}"
    val densityLabel: String get() = when {
        densityDpi >= 560 -> "xxxhdpi"
        densityDpi >= 400 -> "xxhdpi"
        densityDpi >= 320 -> "xhdpi"
        densityDpi >= 240 -> "hdpi"
        densityDpi >= 160 -> "mdpi"
        else -> "ldpi"
    }
}

object DeviceInfo {
    fun getScreenInfo(context: Context): ScreenInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            val metrics = context.resources.displayMetrics
            ScreenInfo(bounds.width(), bounds.height(), metrics.densityDpi)
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            ScreenInfo(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
        }
    }

    /** Returns the wallpaper manager's desired dimensions (accounts for launcher parallax). */
    fun getDesiredWallpaperSize(context: Context): Pair<Int, Int> {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val desiredW = wallpaperManager.desiredMinimumWidth
        val desiredH = wallpaperManager.desiredMinimumHeight
        val screen = getScreenInfo(context)
        return Pair(
            if (desiredW > 0) desiredW else screen.widthPx,
            if (desiredH > 0) desiredH else screen.heightPx
        )
    }
}
