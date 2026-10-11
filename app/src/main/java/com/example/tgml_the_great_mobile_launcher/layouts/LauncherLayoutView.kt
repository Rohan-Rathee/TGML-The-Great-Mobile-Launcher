package com.example.tgml_the_great_mobile_launcher.layouts

import androidx.compose.runtime.Composable
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.LauncherLayout

@Composable
fun LauncherLayoutView(layout: LauncherLayout, apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit)
{
	when (layout) {
		LauncherLayout.RADIAL -> {
			RadialApps(
				apps = apps,
				onAppClick = onAppClick
			)
		}

		LauncherLayout.GRID_SCROLL -> {
			GridScroll(
				apps = apps,
				onAppClick = onAppClick
			)
		}

		LauncherLayout.GRID_ZOOM -> {
			GridZoom(
				apps = apps,
				onAppClick = onAppClick
			)
		}
	}
}