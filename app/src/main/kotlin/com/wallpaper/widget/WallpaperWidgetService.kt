package com.wallpaper.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.widget.RemoteViewsService

/**
 * 위젯 목록 어댑터 서비스 (확장 시 사용 — 현재 구현에서는 단순 RemoteViewsService 스텁).
 */
class WallpaperWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WallpaperRemoteViewsFactory()
    }

    inner class WallpaperRemoteViewsFactory : RemoteViewsFactory {
        override fun onCreate() {}
        override fun onDataSetChanged() {
            // 위젯 전체 갱신
            val manager = AppWidgetManager.getInstance(this@WallpaperWidgetService)
            val ids = manager.getAppWidgetIds(
                ComponentName(this@WallpaperWidgetService, WallpaperWidgetProvider::class.java)
            )
            ids.forEach { WallpaperWidgetProvider.updateWidget(this@WallpaperWidgetService, manager, it) }
        }
        override fun onDestroy() {}
        override fun getCount() = 0
        override fun getViewAt(position: Int) = null
        override fun getLoadingView() = null
        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = position.toLong()
        override fun hasStableIds() = true
    }
}
