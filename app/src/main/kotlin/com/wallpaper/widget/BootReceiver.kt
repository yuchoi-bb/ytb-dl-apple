package com.wallpaper.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 재부팅 후 이전 타이머 초기화 → 시스템 화면꺼짐부터 새로 카운트
            WallpaperPreferences.clearScreenOffTime(context)
            if (WallpaperPreferences.isAutoResetEnabled(context)) {
                ScreenWatchService.start(context)
            }
        }
    }
}
