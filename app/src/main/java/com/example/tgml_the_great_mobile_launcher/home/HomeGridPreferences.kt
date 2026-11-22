package com.example.tgml_the_great_mobile_launcher.home

import android.content.Context
import androidx.core.content.edit

class HomeGridPreferences(context: Context, envId: String = HomeEnvironments.DEFAULT_ID) {
	private val columnsKey = HomeEnvironments.keyFor(KEY_COLUMNS, envId)
	private val rowsKey = HomeEnvironments.keyFor(KEY_ROWS, envId)
	private val preferences = context.applicationContext
		.getSharedPreferences("launcher_home", Context.MODE_PRIVATE)

	fun load(): HomePlacementEngine.GridSize {
		return HomePlacementEngine.GridSize(
			columns = preferences.getInt(columnsKey, DEFAULT_COLUMNS)
				.coerceIn(3, 10),
			rows = preferences.getInt(rowsKey, DEFAULT_ROWS)
				.coerceIn(3, 12)
		)
	}

	fun save(grid: HomePlacementEngine.GridSize) {
		preferences.edit {
			putInt(columnsKey, grid.columns)
			putInt(rowsKey, grid.rows)
		}
	}

	fun clear() {
		preferences.edit {
			remove(columnsKey)
			remove(rowsKey)
		}
	}

	companion object {
		const val DEFAULT_COLUMNS = 5
		const val DEFAULT_ROWS = 6
		private const val KEY_COLUMNS = "home_grid_columns"
		private const val KEY_ROWS = "home_grid_rows"
	}
}