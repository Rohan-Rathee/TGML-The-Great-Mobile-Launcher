
package com.example.tgml_the_great_mobile_launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.LauncherLayout
import com.example.tgml_the_great_mobile_launcher.SideHandleFolder
import com.example.tgml_the_great_mobile_launcher.ui.settings.SettingsHome
import com.example.tgml_the_great_mobile_launcher.ui.settings.SettingsLayout
import com.example.tgml_the_great_mobile_launcher.ui.settings.SettingsPage
import com.example.tgml_the_great_mobile_launcher.ui.settings.SettingsSideHandle
import com.example.tgml_the_great_mobile_launcher.ui.settings.SettingsTransitions
import com.example.tgml_the_great_mobile_launcher.ui.settings.SettingsWallpaper

@Composable
fun LauncherSettings(
	currentLayout: LauncherLayout,
	onLayoutChange: (LauncherLayout) -> Unit,
	currentTransitionStyle: PageTransitionStyle,
	onTransitionStyleChange: (PageTransitionStyle) -> Unit,
	onPickBackground: () -> Unit,
	onUseSystemWallpaper: () -> Unit,
	onOpenSystemWallpaper: () -> Unit,
	onClose: () -> Unit,
	apps: List<AppInfo>,
	sideHandleEnabled: Boolean,
	sideHandleHomeIntegration: Boolean,
	sideHandleFolders: List<SideHandleFolder>,
	onSideHandleEnabledChange: (Boolean) -> Unit,
	onSideHandleHomeIntegrationChange: (Boolean) -> Unit,
	onSideHandleFoldersChange: (List<SideHandleFolder>) -> Unit
) {
	var currentPage by remember { mutableStateOf(SettingsPage.HOME) }

	BackHandler {
		if (currentPage != SettingsPage.HOME) {
			currentPage = SettingsPage.HOME
		} else {
			onClose()
		}
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xFF101010))
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 24.dp, vertical = 20.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				if (currentPage != SettingsPage.HOME) {
					Column {
						Spacer(modifier = Modifier.height(30.dp))
						Text(
							text = "<",
							color = Color.White,
							fontSize = 28.sp,
							modifier = Modifier
								.clickable {
									currentPage = SettingsPage.HOME
								}
								.padding(end = 18.dp)
						)
					}
				}


				Column {
					Spacer(modifier = Modifier.height(40.dp))
					Text(
						text = if (currentPage == SettingsPage.HOME) "Settings" else currentPage.title,
						color = Color.White,
						fontSize = 26.sp
					)

					if (currentPage != SettingsPage.HOME) {
						Text(
							text = currentPage.description,
							color = Color.Gray,
							fontSize = 12.sp
						)
					}
				}
			}

			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 24.dp)
					.padding(bottom = 32.dp)
			) {
				when (currentPage) {
					SettingsPage.HOME -> SettingsHome(
						onNavigate = { currentPage = it }
					)

					SettingsPage.LAYOUT -> SettingsLayout(
						currentLayout = currentLayout,
						onLayoutChange = onLayoutChange
					)

					SettingsPage.WALLPAPER -> SettingsWallpaper(
						onPickBackground = onPickBackground,
						onUseSystemWallpaper = onUseSystemWallpaper,
						onOpenSystemWallpaper = onOpenSystemWallpaper
					)

					SettingsPage.TRANSITIONS -> SettingsTransitions(
						currentTransitionStyle = currentTransitionStyle,
						onTransitionStyleChange = onTransitionStyleChange
					)

					SettingsPage.SIDE_HANDLE -> SettingsSideHandle(
						apps = apps,
						enabled = sideHandleEnabled,
						homeIntegration = sideHandleHomeIntegration,
						folders = sideHandleFolders,
						onEnabledChange = onSideHandleEnabledChange,
						onHomeIntegrationChange = onSideHandleHomeIntegrationChange,
						onFoldersChange = onSideHandleFoldersChange
					)
				}
			}
		}
	}
}

@Composable
fun LayoutOption(
	name: String,
	selected: Boolean,
	enabled: Boolean,
	onClick: () -> Unit
) {
	androidx.compose.foundation.layout.Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.clickable(enabled = enabled, onClick = onClick)
			.padding(vertical = 10.dp)
	) {
		Text(
			text = if (selected) "●" else "○",
			color = if (enabled) Color.White else Color.Gray,
			fontSize = 18.sp
		)

		Text(
			text = name,
			color = if (enabled) Color.White else Color.White.copy(alpha = 0.35f),
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
