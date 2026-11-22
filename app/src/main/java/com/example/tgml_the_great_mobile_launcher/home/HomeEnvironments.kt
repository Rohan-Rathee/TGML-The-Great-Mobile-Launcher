package com.example.tgml_the_great_mobile_launcher.home

import android.appwidget.AppWidgetHost
import android.content.Context
import androidx.core.content.edit
import com.example.tgml_the_great_mobile_launcher.home.widgets.WidgetHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class HomeEnvironment(val id: String, val name: String)

/**
 * Named home screens. Each environment has its own items (apps, widgets, folders) and grid size.
 * The default "Personal" environment uses the original storage keys, so an existing layout is
 * kept without migration. App drawer, settings and notification data are shared across all
 * environments.
 */
object HomeEnvironments {
	const val DEFAULT_ID = "personal"
	private const val DEFAULT_NAME = "Personal"
	private const val MAX_NAME_LENGTH = 24
	private const val PREFS = "launcher_home"
	private const val KEY_LIST = "home_environments"
	private const val KEY_ACTIVE = "home_active_env"

	private val changes = MutableStateFlow(0)
	val version: StateFlow<Int> = changes.asStateFlow()

	/** Storage key for [baseKey] in the given environment. */
	fun keyFor(baseKey: String, envId: String): String =
		if (envId == DEFAULT_ID) baseKey else "$baseKey.$envId"

	private fun prefs(context: Context) =
		context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

	fun list(context: Context): List<HomeEnvironment> =
		listOf(HomeEnvironment(DEFAULT_ID, DEFAULT_NAME)) + readStored(context)

	fun activeId(context: Context): String {
		val id = prefs(context).getString(KEY_ACTIVE, DEFAULT_ID) ?: DEFAULT_ID
		return if (list(context).any { it.id == id }) id else DEFAULT_ID
	}

	fun setActive(context: Context, id: String) {
		if (list(context).none { it.id == id }) return
		prefs(context).edit { putString(KEY_ACTIVE, id) }
		changes.value = changes.value + 1
	}

	fun create(context: Context, name: String): HomeEnvironment? {
		val clean = name.trim().take(MAX_NAME_LENGTH)
		if (clean.isEmpty()) return null
		val environment = HomeEnvironment(UUID.randomUUID().toString().take(8), clean)
		writeStored(context, readStored(context) + environment)
		changes.value = changes.value + 1
		return environment
	}

	/**
	 * Deletes an environment and its layout. Widgets it used are released from the widget host.
	 * The default environment cannot be deleted.
	 */
	fun delete(context: Context, id: String): Boolean {
		if (id == DEFAULT_ID) return false
		val appContext = context.applicationContext

		val host = AppWidgetHost(appContext, WidgetHostController.HOST_ID)
		HomeRepo(appContext, id).loadItems()
			.mapNotNull { it.widgetId }
			.forEach { host.deleteAppWidgetId(it) }

		HomeRepo(appContext, id).clear()
		HomeGridPreferences(appContext, id).clear()
		writeStored(context, readStored(context).filterNot { it.id == id })

		if (prefs(context).getString(KEY_ACTIVE, DEFAULT_ID) == id) {
			prefs(context).edit { putString(KEY_ACTIVE, DEFAULT_ID) }
		}
		changes.value = changes.value + 1
		return true
	}

	private fun readStored(context: Context): List<HomeEnvironment> {
		val json = prefs(context).getString(KEY_LIST, null) ?: return emptyList()
		return try {
			val array = JSONArray(json)
			(0 until array.length()).mapNotNull { index ->
				val obj = array.optJSONObject(index) ?: return@mapNotNull null
				val id = obj.optString("id")
				if (id.isBlank() || id == DEFAULT_ID) return@mapNotNull null
				HomeEnvironment(id, obj.optString("name").ifBlank { "Home" })
			}
		} catch (_: Exception) {
			emptyList()
		}
	}

	private fun writeStored(context: Context, environments: List<HomeEnvironment>) {
		val array = JSONArray()
		environments.forEach { environment ->
			array.put(JSONObject().put("id", environment.id).put("name", environment.name))
		}
		prefs(context).edit { putString(KEY_LIST, array.toString()) }
	}
}