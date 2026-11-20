package com.example.tgml_the_great_mobile_launcher.commandcenter

import android.content.Context
import com.example.tgml_the_great_mobile_launcher.AppInfo
import org.json.JSONArray
import org.json.JSONObject

object AppUsageStore {
	private const val PREFS = "tgml_app_usage"
	private const val KEY_RECENT = "recent"
	private const val KEY_COUNTS = "counts"
	private const val MAX_RECENT = 12

	private fun appKey(app: AppInfo) = "${app.packageName}|${app.activityName}"

	fun recordLaunch(context: Context, app: AppInfo) {
		val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
		val key = appKey(app)
		val recent = readRecent(prefs.getString(KEY_RECENT, null)).toMutableList()
		recent.remove(key)
		recent.add(0, key)
		while (recent.size > MAX_RECENT) recent.removeAt(recent.lastIndex)

		val counts = readCounts(prefs.getString(KEY_COUNTS, null))
		counts[key] = (counts[key] ?: 0) + 1

		prefs.edit()
			.putString(KEY_RECENT, JSONArray(recent).toString())
			.putString(KEY_COUNTS, JSONObject(counts as Map<*, *>).toString())
			.apply()
	}

	fun recent(context: Context, apps: List<AppInfo>, limit: Int = 8): List<AppInfo> {
		val byKey = apps.associateBy(::appKey)
		return readRecent(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_RECENT, null))
			.mapNotNull { byKey[it] }
			.take(limit)
	}

	fun frequent(context: Context, apps: List<AppInfo>, limit: Int = 8): List<AppInfo> {
		val counts = readCounts(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_COUNTS, null))
		val byKey = apps.associateBy(::appKey)
		return counts.entries.sortedByDescending { it.value }
			.mapNotNull { byKey[it.key] }
			.take(limit)
	}

	private fun readRecent(raw: String?): List<String> = runCatching {
		val array = JSONArray(raw ?: "[]")
		(0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
	}.getOrDefault(emptyList())

	private fun readCounts(raw: String?): MutableMap<String, Int> = runCatching {
		val json = JSONObject(raw ?: "{}")
		buildMap {
			val keys = json.keys()
			while (keys.hasNext()) {
				val key = keys.next()
				put(key, json.optInt(key, 0).coerceAtLeast(0))
			}
		}.toMutableMap()
	}.getOrDefault(mutableMapOf())
}
