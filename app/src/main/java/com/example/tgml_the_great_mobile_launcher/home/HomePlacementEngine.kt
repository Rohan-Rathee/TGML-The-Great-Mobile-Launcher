
package com.example.tgml_the_great_mobile_launcher.home

object HomePlacementEngine {
	data class GridSize(
		val columns: Int = 5,
		val rows: Int = 6
	) {
		init {
			require(columns > 0 && rows > 0)
		}
	}

	fun canPlace(
		items: List<HomeItem>,
		page: Int,
		cellX: Int,
		cellY: Int,
		spanX: Int,
		spanY: Int,
		grid: GridSize = GridSize(),
		ignoreItemId: String? = null
	): Boolean {
		if (page < 0 || cellX < 0 || cellY < 0 || spanX <= 0 || spanY <= 0) {
			return false
		}

		if (cellX + spanX > grid.columns || cellY + spanY > grid.rows) {
			return false
		}

		return items.none { item ->
			if (item.id == ignoreItemId || item.page != page) {
				false
			} else {
				val overlapsX =
					cellX < item.cellX + item.spanX &&
							cellX + spanX > item.cellX

				val overlapsY =
					cellY < item.cellY + item.spanY &&
							cellY + spanY > item.cellY

				overlapsX && overlapsY
			}
		}
	}

	fun findFirstAvailable(
		items: List<HomeItem>,
		page: Int,
		spanX: Int = 1,
		spanY: Int = 1,
		grid: GridSize = GridSize()
	): Pair<Int, Int>? {
		for (y in 0..(grid.rows - spanY)) {
			for (x in 0..(grid.columns - spanX)) {
				if (canPlace(items, page, x, y, spanX, spanY, grid)) {
					return x to y
				}
			}
		}

		return null
	}

	fun moveItem(
		items: List<HomeItem>,
		itemId: String,
		page: Int,
		cellX: Int,
		cellY: Int,
		grid: GridSize = GridSize()
	): List<HomeItem>? {
		val item = items.find { it.id == itemId } ?: return null

		if (!canPlace(
				items = items,
				page = page,
				cellX = cellX,
				cellY = cellY,
				spanX = item.spanX,
				spanY = item.spanY,
				grid = grid,
				ignoreItemId = itemId
			)
		) {
			return null
		}

		return items.map { current ->
			if (current.id == itemId) {
				current.copy(
					page = page,
					cellX = cellX,
					cellY = cellY
				)
			} else {
				current
			}
		}
	}
}
