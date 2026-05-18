package com.wallpaper.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WallpaperWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_wallpaper)

            // "앱 열기" 버튼 — 메인 액티비티 실행
            val openAppIntent = PendingIntent.getActivity(
                context, widgetId,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_open_app, openAppIntent)

            // "배경화면 선택" 버튼 — 피커 액티비티 바로 실행
            val pickIntent = PendingIntent.getActivity(
                context, widgetId + 1000,
                Intent(context, WallpaperPickerActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_pick_wallpaper, pickIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
