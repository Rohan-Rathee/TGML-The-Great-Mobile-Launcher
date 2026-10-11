package com.example.tgml_the_great_mobile_launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.LauncherLayout

@Composable
fun LauncherSettings(currentLayout: LauncherLayout, onLayoutChange: (LauncherLayout) -> Unit, onClose: () -> Unit)
{
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.65f))
	) {
		Column(
			modifier = Modifier
				.align(Alignment.Center)
				.background(
					Color.Black.copy(alpha = 0.9f),
					RoundedCornerShape(24.dp)
				)
				.padding(24.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "Launcher Layout",
				color = Color.White,
				fontSize = 22.sp
			)

			LayoutOption(
				name = "Radial",
				selected = currentLayout == LauncherLayout.RADIAL,
				enabled = true,
				onClick = {
					onLayoutChange(LauncherLayout.RADIAL)
				}
			)

			LayoutOption(
				name = "Grid Scroll",
				selected = currentLayout == LauncherLayout.GRID_SCROLL,
				enabled = true,
				onClick = {
					onLayoutChange(LauncherLayout.GRID_SCROLL)
				}
			)

			LayoutOption(
				name = "Grid Zoom",
				selected = currentLayout == LauncherLayout.GRID_ZOOM,
				enabled = true,
				onClick = {
					onLayoutChange(LauncherLayout.GRID_ZOOM)
				}
			)

			Text(
				text = "Close",
				color = Color.White,
				modifier = Modifier
					.clickable {
						onClose()
					}
					.padding(8.dp)
			)
		}
	}
}

@Composable
fun LayoutOption(name: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit)
{
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.clickable(enabled = enabled) {
				onClick()
			}
			.padding(vertical = 10.dp)
	) {
		Text(
			text = if (selected) "●" else "○",
			color = if (enabled) Color.White else Color.Gray,
			fontSize = 18.sp
		)

		Text(
			text = name,
			color = if (enabled) {
				Color.White
			} else {
				Color.White.copy(alpha = 0.35f)
			},
			fontSize = 16.sp,
			modifier = Modifier.padding(start = 12.dp)
		)

		if (!enabled) {
			Text(
				text = "Coming soon",
				color = Color.White.copy(alpha = 0.3f),
				fontSize = 11.sp,
				modifier = Modifier.padding(start = 8.dp)
			)
		}
	}
}
