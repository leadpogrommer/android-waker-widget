package com.github.leadpogrommer.waker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews


class WakerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        Log.d("WW", "update from WakerWidget")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        Log.d("WW", "restored")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            WidgetPrefs.deleteWidgetPref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

            val machineName = WidgetPrefs.loadWidgetPref(context, appWidgetId)?.name
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.waker_widget)
            views.setTextViewText(R.id.machine_name, machineName)
//            views.setOnClickPendingIntent(R.id.wake_button, PendingIntent.getService(context, appWidgetId, WakeService.newIntent(context, appWidgetId),0))
            views.setOnClickPendingIntent(
                R.id.wake_button,
                PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    WakeReceiver.newIntent(context, appWidgetId),
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            )


            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

