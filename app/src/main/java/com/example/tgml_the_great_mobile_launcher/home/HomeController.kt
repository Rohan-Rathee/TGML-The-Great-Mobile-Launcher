
package com.example.tgml_the_great_mobile_launcher.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf

class HomeController(context: Context) {
	private val repository = HomeRepo(context)

	val items = mutableStateListOf<HomeItem>().apply {
		addAll(repository.loadItems())
	}

	fun itemsOnPage(page: Int): List<HomeItem> =
		items.filter { it.page == page }

	fun addAppShortcut(
		packageName: String,
		activityName: String,
		page: Int,
		grid: HomePlacementEngine.GridSize = HomePlacementEngine.GridSize()
	): Boolean {
		if (items.any {
				it.type == HomeItemType.APP_SHORTCUT &&
						it.packageName == packageName &&
						it.activityName == activityName
			}
		) return false

		val position = HomePlacementEngine.findFirstAvailable(
			items = items,
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

	fun moveItem(
		itemId: String,
		page: Int,
		cellX: Int,
		cellY: Int,
		grid: HomePlacementEngine.GridSize = HomePlacementEngine.GridSize()
	): Boolean {
		val updated = HomePlacementEngine.moveItem(
			items = items.toList(),
			itemId = itemId,
			page = page,
			cellX = cellX,
			cellY = cellY,
			grid = grid
		) ?: return false

		items.clear()
		items.addAll(updated)
		save()
		return true
	}

	fun removeItem(itemId: String): Boolean {
		val removed = items.removeAll { it.id == itemId }
		if (removed) save()
		return removed
	}

	fun save() {
		repository.saveItems(items.toList())
	}
}
