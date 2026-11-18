
package com.example.tgml_the_great_mobile_launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tgml_the_great_mobile_launcher.LauncherLayout
import com.example.tgml_the_great_mobile_launcher.ui.LayoutOption


@Composable
fun SettingsLayout(
	currentLayout: LauncherLayout,
	onLayoutChange: (LauncherLayout) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color(0xFF202020))
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(2.dp)
	) {
		LayoutOption(
			name = "Radial",
			selected = currentLayout == LauncherLayout.RADIAL,
			enabled = true,
			onClick = { onLayoutChange(LauncherLayout.RADIAL) }
		)

		LayoutOption(
			name = "Grid Scroll",
			selected = currentLayout == LauncherLayout.GRID_SCROLL,
			enabled = true,
			onClick = { onLayoutChange(LauncherLayout.GRID_SCROLL) }
		)

		LayoutOption(
			name = "Grid Zoom",
			selected = currentLayout == LauncherLayout.GRID_ZOOM,
			enabled = true,
			onClick = { onLayoutChange(LauncherLayout.GRID_ZOOM) }
		)

		LayoutOption(
			name = "Multi-page",
			selected = currentLayout == LauncherLayout.MULTI_PAGE,
			enabled = true,
			onClick = { onLayoutChange(LauncherLayout.MULTI_PAGE) }
		)
	}
}
