
package com.example.tgml_the_great_mobile_launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.SideHandleFolder
import com.example.tgml_the_great_mobile_launcher.ui.SideHandleSettings


@Composable
fun SettingsSideHandle(
	apps: List<AppInfo>,
	enabled: Boolean,
	homeIntegration: Boolean,
	folders: List<SideHandleFolder>,
	onEnabledChange: (Boolean) -> Unit,
	onHomeIntegrationChange: (Boolean) -> Unit,
	onFoldersChange: (List<SideHandleFolder>) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color(0xFF202020))
			.padding(16.dp)
	) {
		SideHandleSettings(
			apps = apps,
			enabled = enabled,
			homeIntegration = homeIntegration,
			folders = folders,
			onEnabledChange = onEnabledChange,
			onHomeIntegrationChange = onHomeIntegrationChange,
			onFoldersChange = onFoldersChange
		)
	}
}
