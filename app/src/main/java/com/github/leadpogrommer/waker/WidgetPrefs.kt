package com.github.leadpogrommer.waker

import android.content.Context
import java.io.Serializable

class WidgetPrefs : Serializable {
    var name: String? = null
    var mac: String? = null

    companion object {
        private const val PREFS_NAME = "com.github.leadpogrommer.waker.WakerWidget"
        private const val PREF_PREFIX_KEY = "appwidget_"


        internal fun saveWidgetPref(context: Context, appWidgetId: Int, data: WidgetPrefs) {
            val string = data.mac + data.name


            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, string)
            prefs.apply()
        }

        internal fun loadWidgetPref(context: Context, appWidgetId: Int): WidgetPrefs? {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val string = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)

            string ?: return null

            val data = WidgetPrefs()
            data.mac = string.slice(0..16)
            data.name = string.drop(17)

            return data

        }

        internal fun deleteWidgetPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }

}