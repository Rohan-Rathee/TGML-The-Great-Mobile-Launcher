package com.example.tgml_the_great_mobile_launcher.home

import android.content.Context
import androidx.core.content.edit

class HomeGridPreferences(context: Context) {
	private val preferences = context.applicationContext
		.getSharedPreferences("launcher_home", Context.MODE_PRIVATE)

	fun load(): HomePlacementEngine.GridSize {
		return HomePlacementEngine.GridSize(
			columns = preferences.getInt(KEY_COLUMNS, DEFAULT_COLUMNS)
				.coerceIn(3, 10),
			rows = preferences.getInt(KEY_ROWS, DEFAULT_ROWS)
				.coerceIn(3, 12)
		)
	}

	fun save(grid: HomePlacementEngine.GridSize) {
		preferences.edit {
			putInt(KEY_COLUMNS, grid.columns)
			putInt(KEY_ROWS, grid.rows)
		}
	}

	companion object {
		const val DEFAULT_COLUMNS = 5
		const val DEFAULT_ROWS = 6
		private const val KEY_COLUMNS = "home_grid_columns"
		private const val KEY_ROWS = "home_grid_rows"
	}
}