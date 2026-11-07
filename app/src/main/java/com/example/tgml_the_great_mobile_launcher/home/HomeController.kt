
package com.example.tgml_the_great_mobile_launcher.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf

class HomeController(context: Context) {
	private val repository = HomeRepo(context)
	private val gridPreferences = HomeGridPreferences(context)

	var grid: HomePlacementEngine.GridSize = gridPreferences.load()
		private set

	val items = mutableStateListOf<HomeItem>().apply {
		addAll(repository.loadItems())
	}

	fun itemsOnPage(page: Int): List<HomeItem> =
		items.filter { it.page == page && it.parentFolderId == null }

	fun itemsInFolder(folderId: String): List<HomeItem> =
		items.filter { it.parentFolderId == folderId }

	fun addAppShortcut(
		packageName: String,
		activityName: String,
		page: Int,
		grid: HomePlacementEngine.GridSize = this.grid
	): Boolean {
		if (items.any {
				it.type == HomeItemType.APP_SHORTCUT &&
						it.packageName == packageName &&
						it.activityName == activityName
			}) return false

		val position = HomePlacementEngine.findFirstAvailable(
			items = items.filter { it.parentFolderId == null },
			page = page,
			grid = grid
		) ?: return false

		items.add(
			HomeItem.appShortcut(
				packageName = packageName,
				activityName = activityName,
				page = page,
				cellX = position.first,
				cellY = position.second
			)
		)
		save()
		return true
	}

	fun addWidget(
		widgetId: Int,
		page: Int,
		spanX: Int = 2,
		spanY: Int = 2
	): Boolean {
		if (widgetId < 0 || spanX <= 0 || spanY <= 0) return false
		if (spanX > grid.columns || spanY > grid.rows) return false
		if (items.any { it.widgetId == widgetId }) return false

		val position = HomePlacementEngine.findFirstAvailable(
			items = items.filter { it.parentFolderId == null },
			page = page,
			spanX = spanX,
			spanY = spanY,
			grid = grid
		) ?: return false

		items.add(
			HomeItem.widget(
				widgetId = widgetId,
				page = page,
				cellX = position.first,
				cellY = position.second,
				spanX = spanX,
				spanY = spanY
			)
		)
		save()
		return true
	}

	fun resizeWidget(widgetItemId: String, spanX: Int, spanY: Int): Boolean {
		if (spanX <= 0 || spanY <= 0) return false
		val roots = items.filter { it.parentFolderId == null }
		val resized = HomePlacementEngine.resizeItem(
			items = roots,
			itemId = widgetItemId,
			spanX = spanX,
			spanY = spanY,
			grid = grid
		) ?: return false

		val updated = resized.associateBy { it.id }
		for (index in items.indices) {
			updated[items[index].id]?.let { items[index] = it }
		}

		save()
		return true
	}

	fun widgetIdForItem(itemId: String): Int? =
		items.firstOrNull {
			it.id == itemId && it.type == HomeItemType.WIDGET
		}?.widgetId

	fun moveItem(
		itemId: String,
		page: Int,
		cellX: Int,
		cellY: Int,
		grid: HomePlacementEngine.GridSize = this.grid
	): Boolean {
		val item = items.find { it.id == itemId } ?: return false
		if (item.parentFolderId != null) return false

		val rootItems = items.filter { it.parentFolderId == null }
		val updatedRoots = HomePlacementEngine.moveItem(
			items = rootItems,
			itemId = itemId,
			page = page,
			cellX = cellX,
			cellY = cellY,
			grid = grid
		) ?: return false

		val updatedById = updatedRoots.associateBy { it.id }
		for (index in items.indices) {
			updatedById[items[index].id]?.let { items[index] = it }
		}

		save()
		return true
	}

	fun removeItem(itemId: String): Boolean {
		val item = items.find { it.id == itemId } ?: return false

		if (item.type == HomeItemType.FOLDER) {
			items.removeAll {
				it.id == itemId || it.parentFolderId == itemId
			}
		} else {
			items.remove(item)
			item.parentFolderId?.let(::normalizeFolder)
		}

		save()
		return true
	}

	fun createFolder(
		firstItemId: String,
		secondItemId: String,
		name: String = "Folder"
	): String? {
		val first = items.find { it.id == firstItemId } ?: return null
		val second = items.find { it.id == secondItemId } ?: return null

		if (first.id == second.id ||
			first.type != HomeItemType.APP_SHORTCUT ||
			second.type != HomeItemType.APP_SHORTCUT ||
			first.parentFolderId != null ||
			second.parentFolderId != null ||
			first.page != second.page
		) return null

		val folder = HomeItem.folder(
			name = name,
			page = first.page,
			cellX = first.cellX,
			cellY = first.cellY
		)

		items.removeAll { it.id == first.id || it.id == second.id }
		items.add(folder)
		items.add(first.copy(parentFolderId = folder.id, cellX = 0, cellY = 0))
		items.add(second.copy(parentFolderId = folder.id, cellX = 0, cellY = 0))

		save()
		return folder.id
	}

	fun renameFolder(folderId: String, name: String): Boolean {
		val cleanName = name.trim()
		if (cleanName.isBlank()) return false

		val index = items.indexOfFirst {
			it.id == folderId && it.type == HomeItemType.FOLDER
		}
		if (index == -1) return false

		items[index] = items[index].copy(folderName = cleanName)
		save()
		return true
	}

	fun moveItemIntoFolder(itemId: String, folderId: String): Boolean {
		val item = items.find { it.id == itemId } ?: return false
		val folder = items.find {
			it.id == folderId && it.type == HomeItemType.FOLDER
		} ?: return false

		if (item.type != HomeItemType.APP_SHORTCUT ||
			item.parentFolderId != null ||
			item.id == folder.id
		) return false

		val index = items.indexOfFirst { it.id == itemId }
		items[index] = item.copy(
			parentFolderId = folder.id,
			cellX = 0,
			cellY = 0,
			page = folder.page
		)
		save()
		return true
	}

	fun moveItemOutOfFolder(itemId: String, page: Int): Boolean {
		val item = items.find { it.id == itemId } ?: return false
		val folderId = item.parentFolderId ?: return false

		val position = HomePlacementEngine.findFirstAvailable(
			items = items.filter { it.parentFolderId == null },
			page = page,
			spanX = item.spanX,
			spanY = item.spanY,
			grid = grid
		) ?: return false

		val index = items.indexOfFirst { it.id == itemId }
		items[index] = item.copy(
			parentFolderId = null,
			page = page,
			cellX = position.first,
			cellY = position.second
		)

		normalizeFolder(folderId)
		save()
		return true
	}

	private fun normalizeFolder(folderId: String) {
		val folder = items.find {
			it.id == folderId && it.type == HomeItemType.FOLDER
		} ?: return

		val children = items.filter { it.parentFolderId == folderId }

		when (children.size) {
			0 -> items.remove(folder)
			1 -> {
				val child = children.first()
				val index = items.indexOfFirst { it.id == child.id }

				if (index >= 0) {
					items[index] = child.copy(
						parentFolderId = null,
						page = folder.page,
						cellX = folder.cellX,
						cellY = folder.cellY
					)
				}
				items.remove(folder)
			}
		}
	}

	fun updateGrid(columns: Int, rows: Int): Boolean {
		val newGrid = runCatching {
			HomePlacementEngine.GridSize(columns, rows)
		}.getOrNull() ?: return false

		val updated = items.toMutableList()
		val rootItems = updated.filter { it.parentFolderId == null }
		val placed = mutableListOf<HomeItem>()

		for (item in rootItems) {
			val position = if (
				HomePlacementEngine.canPlace(
					items = placed,
					page = item.page,
					cellX = item.cellX,
					cellY = item.cellY,
					spanX = item.spanX,
					spanY = item.spanY,
					grid = newGrid
				)
			) {
				item.cellX to item.cellY
			} else {
				HomePlacementEngine.findFirstAvailable(
					items = placed,
					page = item.page,
					spanX = item.spanX,
					spanY = item.spanY,
					grid = newGrid
				) ?: return false
			}

			placed.add(
				item.copy(
					cellX = position.first,
					cellY = position.second
				)
			)
		}

		for (item in placed) {
			val index = updated.indexOfFirst { it.id == item.id }
			if (index >= 0) updated[index] = item
		}

		items.clear()
		items.addAll(updated)
		grid = newGrid
		gridPreferences.save(newGrid)
		save()
		return true
	}

	fun save() {
		repository.saveItems(items.toList())
	}
}
