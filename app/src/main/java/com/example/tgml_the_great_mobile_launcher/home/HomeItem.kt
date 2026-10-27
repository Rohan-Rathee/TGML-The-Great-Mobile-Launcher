
package com.example.tgml_the_great_mobile_launcher.home

import java.util.UUID

enum class HomeItemType {
		APP_SHORTCUT,
	WIDGET,
	FOLDER
}

data class HomeItem(
	val id: String = UUID.randomUUID().toString(),
	val type: HomeItemType,
	val page: Int = 0,
	val cellX: Int = 0,
	val cellY: Int = 0,
	val spanX: Int = 1,
	val spanY: Int = 1,
	val packageName: String? = null,
	val activityName: String? = null,
	val widgetId: Int? = null
) {
	init {
		require(page >= 0) { "Page cannot be negative" }
		require(cellX >= 0 && cellY >= 0) { "Cell coordinates cannot be negative" }
		require(spanX > 0 && spanY > 0) { "Item dimensions must be positive" }
	}

	companion object {
		fun appShortcut(
			packageName: String,
			activityName: String,
			page: Int = 0,
			cellX: Int = 0,
			cellY: Int = 0
		) = HomeItem(
			type = HomeItemType.APP_SHORTCUT,
			page = page,
			cellX = cellX,
			cellY = cellY,
			packageName = packageName,
			activityName = activityName
		)

		fun widget(
			widgetId: Int,
			page: Int,
			cellX: Int,
			cellY: Int,
			spanX: Int = 2,
			spanY: Int = 2
		) = HomeItem(
			type = HomeItemType.WIDGET,
			page = page,
			cellX = cellX,
			cellY = cellY,
			spanX = spanX,
			spanY = spanY,
			widgetId = widgetId
		)
	}
}
