package com.wallpaper.widget

import android.content.Context
import android.content.SharedPreferences

object WallpaperPreferences {

    private const val PREFS_NAME = "wallpaper_prefs"
    private const val KEY_DEFAULT_WALLPAPER_PATH = "default_wallpaper_path"
    private const val KEY_SCREEN_OFF_TIME = "screen_off_time"
    private const val KEY_AUTO_RESET_ENABLED = "auto_reset_enabled"

    const val SCREEN_OFF_THRESHOLD_MS = 2 * 60 * 1000L // 2분

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveDefaultWallpaperPath(context: Context, path: String) {
        prefs(context).edit().putString(KEY_DEFAULT_WALLPAPER_PATH, path).apply()
    }

    fun getDefaultWallpaperPath(context: Context): String? =
        prefs(context).getString(KEY_DEFAULT_WALLPAPER_PATH, null)

    fun hasDefaultWallpaper(context: Context): Boolean =
        getDefaultWallpaperPath(context) != null

    fun setScreenOffTime(context: Context, timeMs: Long) {
        prefs(context).edit().putLong(KEY_SCREEN_OFF_TIME, timeMs).apply()
    }

    fun getScreenOffTime(context: Context): Long =
        prefs(context).getLong(KEY_SCREEN_OFF_TIME, 0L)

    fun clearScreenOffTime(context: Context) = setScreenOffTime(context, 0L)

    fun setAutoResetEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_RESET_ENABLED, enabled).apply()
    }

    fun isAutoResetEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_RESET_ENABLED, true)

    fun shouldResetWallpaper(context: Context): Boolean {
        if (!isAutoResetEnabled(context)) return false
        if (!hasDefaultWallpaper(context)) return false
        val offTime = getScreenOffTime(context)
        if (offTime == 0L) return false
        return System.currentTimeMillis() - offTime >= SCREEN_OFF_THRESHOLD_MS
    }
}
