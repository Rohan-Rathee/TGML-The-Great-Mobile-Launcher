package com.example.tgml_the_great_mobile_launcher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SideHandleFolder(
	val id: String,
	val name: String,
	val packages: List<String>,
	val color: Int = SideHandlePreferences.DEFAULT_ARC_COLOR
)

object SideHandlePreferences {
	private const val PREFS = "side_handle"
	private const val KEY_ENABLED = "enabled"
	private const val KEY_HOME_INTEGRATION = "home_integration"
	private const val KEY_FOLDERS = "folders"
	private const val KEY_DEFAULT_ARC_COLOR = "default_arc_color"
	private const val KEY_RIGHT_SIDE = "right_side"
	const val DEFAULT_ARC_COLOR = -11309570

	val HANDLE_COLORS = listOf(
		0xFF536DFE.toInt(),
		0xFF00BFA5.toInt(),
		0xFFFF7043.toInt(),
		0xFFFFCA28.toInt(),
		0xFFAB47BC.toInt(),
		0xFFEC407A.toInt(),
		0xFF26A69A.toInt(),
		0xFF78909C.toInt(),
		0xFFFFFFFF.toInt()
	)

	fun isEnabled(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)
	fun setEnabled(context: Context, enabled: Boolean) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
	fun homeIntegration(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_HOME_INTEGRATION, false)
	fun setHomeIntegration(context: Context, enabled: Boolean) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_HOME_INTEGRATION, enabled).apply()

	fun isRightSide(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_RIGHT_SIDE, true)
	fun setRightSide(context: Context, right: Boolean) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_RIGHT_SIDE, right).apply()

	fun defaultArcColor(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_DEFAULT_ARC_COLOR, DEFAULT_ARC_COLOR)
	fun setDefaultArcColor(context: Context, color: Int) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_DEFAULT_ARC_COLOR, color).apply()

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
				SideHandleFolder(
					item.getString("id"),
					item.getString("name"),
					(0 until packages.length()).map { packages.getString(it) },
					item.optInt("color", DEFAULT_ARC_COLOR)
				)
			}
		} catch (_: Exception) { emptyList() }
	}

	fun saveFolders(context: Context, folders: List<SideHandleFolder>) {
		val array = JSONArray()
		folders.forEach { folder ->
			val packages = JSONArray()
			folder.packages.forEach { packages.put(it) }
			array.put(
				JSONObject()
					.put("id", folder.id)
					.put("name", folder.name)
					.put("packages", packages)
					.put("color", folder.color)
			)
		}
		context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_FOLDERS, array.toString()).apply()
	}
}