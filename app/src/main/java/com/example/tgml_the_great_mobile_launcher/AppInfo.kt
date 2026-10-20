package com.example.tgml_the_great_mobile_launcher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

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

fun Drawable.toSafeImageBitmap(width: Int = 128, height: Int = 128): ImageBitmap {
	return try {
		toBitmap(width = width, height = height).asImageBitmap()
	} catch (_: Throwable) {
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		try {
			val canvas = Canvas(bitmap)
			setBounds(0, 0, width, height)
			draw(canvas)
		} catch (_: Throwable) { }
		bitmap.asImageBitmap()
	}
}

