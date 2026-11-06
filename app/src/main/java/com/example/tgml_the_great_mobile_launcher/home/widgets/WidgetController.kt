
package com.example.tgml_the_great_mobile_launcher.home.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Bundle

class WidgetHostController(context: Context) {
	companion object {
		private const val HOST_ID = 1024
	}

	private val appContext = context.applicationContext

	val manager: AppWidgetManager =
		AppWidgetManager.getInstance(appContext)

	private val host = AppWidgetHost(appContext, HOST_ID)

	fun startListening() {
		host.startListening()
	}

	fun stopListening() {
		host.stopListening()
	}

	fun getInstalledProviders(): List<AppWidgetProviderInfo> {
		return manager.installedProviders
	}

	fun allocateWidgetId(): Int {
		return host.allocateAppWidgetId()
	}

	fun bindWidget(
		widgetId: Int,
		provider: ComponentName,
		options: Bundle? = null
	): Boolean {
		return manager.bindAppWidgetIdIfAllowed(
			widgetId,
			provider,
			options
		)
	}

	fun createWidgetView(
		context: Context,
		widgetId: Int
	): AppWidgetHostView? {
		val providerInfo = manager.getAppWidgetInfo(widgetId)
			?: return null

		return host.createView(
			context,
			widgetId,
			providerInfo
		)
	}

	fun deleteWidget(widgetId: Int) {
		host.deleteAppWidgetId(widgetId)
	}
}
