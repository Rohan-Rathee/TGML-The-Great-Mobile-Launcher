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
	val widgetId: Int? = null,
	val folderName: String? = null,
	val parentFolderId: String? = null
) {
	init {
		require(page >= 0) { "Page cannot be negative" }
		require(cellX >= 0 && cellY >= 0) { "Cell coordinates cannot be negative" }
		require(spanX > 0 && spanY > 0) { "Item dimensions must be positive" }

		if (type == HomeItemType.APP_SHORTCUT) {
			require(!packageName.isNullOrBlank()) { "App shortcut requires a package name" }
			require(!activityName.isNullOrBlank()) { "App shortcut requires an activity name" }
		}

		if (type == HomeItemType.FOLDER) {
			require(parentFolderId == null) { "A folder cannot be inside another folder" }
		}
	}

	companion object {
		fun appShortcut(
			packageName: String,
			activityName: String,
			page: Int = 0,
			cellX: Int = 0,
			cellY: Int = 0,
			parentFolderId: String? = null
		) = HomeItem(
			type = HomeItemType.APP_SHORTCUT,
			page = page,
			cellX = cellX,
			cellY = cellY,
			packageName = packageName,
			activityName = activityName,
			parentFolderId = parentFolderId
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

		fun folder(
			name: String = "Folder",
			page: Int,
			cellX: Int,
			cellY: Int
		) = HomeItem(
			type = HomeItemType.FOLDER,
			page = page,
			cellX = cellX,
			cellY = cellY,
			folderName = name
		)
	}
}