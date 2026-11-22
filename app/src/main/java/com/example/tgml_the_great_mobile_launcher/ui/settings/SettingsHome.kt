
package com.example.tgml_the_great_mobile_launcher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingsHome(
	onNavigate: (SettingsPage) -> Unit
) {
	Column(
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Text(
			text = "Access and Change how your launcher looks",
			color = Color.Gray,
			fontSize = 14.sp
		)

		Spacer(modifier = Modifier.height(9.dp))

		SettingsCategoryCard(
			title = "Launcher Layout",
			description = "Layout options?",
			onClick = { onNavigate(SettingsPage.LAYOUT) }
		)

		SettingsCategoryCard(
			title = "Wallpaper",
			description = "Umm, change wallpaper?",
			onClick = { onNavigate(SettingsPage.WALLPAPER) }
		)

		SettingsCategoryCard(
			title = "Page Transitions",
			description = "No way does this do exactly what it says",
			onClick = { onNavigate(SettingsPage.TRANSITIONS) }
		)

		SettingsCategoryCard(
			title = "App Lock",
			description = "Lock, hide and protect apps",
			onClick = { onNavigate(SettingsPage.APP_LOCK) }
		)

		SettingsCategoryCard(
			title = "Notifications",
			description = "Badges, notification center and gesture",
			onClick = { onNavigate(SettingsPage.NOTIFICATIONS) }
		)

		SettingsCategoryCard(
			title = "Side Handle",
			description = "Ok, these descriptions go away the next to next update since everything is what it's title says. You have been integral to the development of this launcher if you can read this",
			onClick = { onNavigate(SettingsPage.SIDE_HANDLE) }
		)
	}
}
