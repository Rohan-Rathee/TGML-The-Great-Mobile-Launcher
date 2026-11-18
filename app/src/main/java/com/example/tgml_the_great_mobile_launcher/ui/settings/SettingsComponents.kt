
package com.example.tgml_the_great_mobile_launcher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


enum class SettingsPage(
	val title: String,
	val description: String
) {
	HOME("Settings", "Configure TGML"),
	LAYOUT("Launcher Layout", "Choose how your apps are displayed"),
	WALLPAPER("Wallpaper", "Customise your launcher background"),
	TRANSITIONS("Page Transitions", "Customise page animations"),
	SIDE_HANDLE("Side Handle", "Configure your side handle"),
}

@Composable
fun SettingsCategoryCard(
	title: String,
		description: String,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(18.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(5.dp)
		) {
			Text(
				text = title,
				color = Color.White,
				fontSize = 17.sp
			)

			Text(
				text = description,
				color = Color.Gray,
				fontSize = 13.sp
			)
		}

		Text(
			text = ">",
			color = Color.White,
			fontSize = 28.sp
		)
	}
}
