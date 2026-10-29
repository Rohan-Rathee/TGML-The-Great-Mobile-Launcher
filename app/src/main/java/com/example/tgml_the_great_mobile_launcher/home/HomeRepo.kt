package com.example.tgml_the_great_mobile_launcher.home

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class HomeRepo(context: Context) {
	private val preferences = context.applicationContext
		.getSharedPreferences("launcher_home", Context.MODE_PRIVATE)


	fun loadItems(): List<HomeItem> {
		val json = preferences.getString(KEY_ITEMS, null) ?: return emptyList()

		return try {
			val array = JSONArray(json)
			buildList {
				for (i in 0 until array.length()) {
					val item = array.optJSONObject(i) ?: continue

					val type = runCatching {
						HomeItemType.valueOf(item.getString("type"))
					}.getOrNull() ?: continue

					val packageName = item.optString("packageName")
						.takeIf { it.isNotBlank() }

					val activityName = item.optString("activityName")
						.takeIf { it.isNotBlank() }

					val widgetId = if (item.has("widgetId") && !item.isNull("widgetId")) {
						item.optInt("widgetId", -1).takeIf { it >= 0 }
					} else {
						null
					}

					add(
						HomeItem(
							id = item.optString("id"),
							type = type,
							page = item.optInt("page", 0).coerceAtLeast(0),
							cellX = item.optInt("cellX", 0).coerceAtLeast(0),
							cellY = item.optInt("cellY", 0).coerceAtLeast(0),
							spanX = item.optInt("spanX", 1).coerceAtLeast(1),
							spanY = item.optInt("spanY", 1).coerceAtLeast(1),
							packageName = packageName,
							activityName = activityName,
							widgetId = widgetId,
							folderName = item.optString("folderName")
								.takeIf { it.isNotBlank() },
							parentFolderId = item.optString("parentFolderId")
								.takeIf { it.isNotBlank() }
						)
					)
				}
			}
		} catch (_: Exception) {
			emptyList()
		}
	}

	fun saveItems(items: List<HomeItem>) {
		val array = JSONArray()

		items.forEach { item ->
			array.put(
				JSONObject().apply {
					put("id", item.id)
					put("type", item.type.name)
					put("page", item.page)
					put("cellX", item.cellX)
					put("cellY", item.cellY)
					put("spanX", item.spanX)
					put("spanY", item.spanY)
					put("packageName", item.packageName)
					put("activityName", item.activityName)
					put("widgetId", item.widgetId)
					put("folderName", item.folderName)
					put("parentFolderId", item.parentFolderId)
				}
			)
		}

		preferences.edit {
			putString(KEY_ITEMS, array.toString())
		}
	}

	companion object {
		private const val KEY_ITEMS = "home_items"
	}
}