package com.example.tgml_the_great_mobile_launcher

import android.graphics.drawable.Drawable

data class AppInfo(
	val name: String,
	val packageName: String,
	val activityName: String,
	val icon: Drawable
)

enum class LauncherLayout
{
	RADIAL,
	GRID_SCROLL,
	GRID_ZOOM,
}

