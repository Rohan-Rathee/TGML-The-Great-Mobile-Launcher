package com.example.tgml_the_great_mobile_launcher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SideHandleFolder(
	val id: String,
	val name: String,
	val packages: List<String>
)

object SideHandlePreferences {
	private const val PREFS = "side_handle"
	private const val KEY_ENABLED = "enabled"
	private const val KEY_HOME_INTEGRATION = "home_integration"
	private const val KEY_FOLDERS = "folders"

	fun isEnabled(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)
	fun setEnabled(context: Context, enabled: Boolean) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
	fun homeIntegration(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_HOME_INTEGRATION, false)
	fun setHomeIntegration(context: Context, enabled: Boolean) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_HOME_INTEGRATION, enabled).apply()

	fun loadFolders(context: Context): List<SideHandleFolder> {
		val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FOLDERS, null)
		if (raw == null) return listOf(
			SideHandleFolder("communication", "Communication", emptyList()),
			SideHandleFolder("productivity", "Productivity", emptyList()),
			SideHandleFolder("entertainment", "Entertainment", emptyList()),
			SideHandleFolder("games", "Games", emptyList()),
			SideHandleFolder("tools", "Tools", emptyList())
		)
		return try {
			val array = JSONArray(raw)
			(0 until array.length()).map { index ->
				val item = array.getJSONObject(index)
				val packages = item.optJSONArray("packages") ?: JSONArray()
				SideHandleFolder(item.getString("id"), item.getString("name"), (0 until packages.length()).map { packages.getString(it) })
			}
		} catch (_: Exception) { emptyList() }
	}

	fun saveFolders(context: Context, folders: List<SideHandleFolder>) {
		val array = JSONArray()
		folders.forEach { folder ->
			val packages = JSONArray()
			folder.packages.forEach { packages.put(it) }
			array.put(JSONObject().put("id", folder.id).put("name", folder.name).put("packages", packages))
		}
		context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_FOLDERS, array.toString()).apply()
	}
}