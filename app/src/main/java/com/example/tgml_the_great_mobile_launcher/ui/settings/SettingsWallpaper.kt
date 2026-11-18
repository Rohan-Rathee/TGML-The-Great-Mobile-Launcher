
package com.example.tgml_the_great_mobile_launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.ui.LayoutOption


@Composable
fun SettingsWallpaper(
	onPickBackground: () -> Unit,
	onUseSystemWallpaper: () -> Unit,
	onOpenSystemWallpaper: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color(0xFF202020))
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			text = "Wallpaper source",
			color = Color.White,
			fontSize = 17.sp
		)

		LayoutOption(
			name = "Choose App Wallpaper",
			selected = false,
			enabled = true,
			onClick = onPickBackground
		)

		LayoutOption(
			name = "Use Phone Wallpaper",
			selected = false,
			enabled = true,
			onClick = onUseSystemWallpaper
		)

		LayoutOption(
			name = "Change Phone Wallpaper",
			selected = false,
			enabled = true,
			onClick = onOpenSystemWallpaper
		)
	}
}
