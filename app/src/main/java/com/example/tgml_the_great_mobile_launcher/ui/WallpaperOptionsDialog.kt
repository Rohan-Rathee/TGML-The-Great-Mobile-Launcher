
package com.example.tgml_the_great_mobile_launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun WallpaperOptionsDialog(
	onPickBackground: () -> Unit,
	onUseSystemWallpaper: () -> Unit,
	onOpenSystemWallpaper: () -> Unit,
	onClose: () -> Unit
) {
	Dialog(onDismissRequest = onClose) {
		Column(
			modifier = Modifier
				.background(Color.Black.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
				.padding(24.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = "Wallpaper",
				color = Color.White,
				fontSize = 22.sp
			)

			LayoutOption("Choose App Wallpaper", false, true) {
				onPickBackground()
				onClose()
			}

			LayoutOption("Use Phone Wallpaper", false, true) {
				onUseSystemWallpaper()
				onClose()
			}

			LayoutOption("Change Phone Wallpaper", false, true) {
				onClose()
				onOpenSystemWallpaper()
			}

			LayoutOption("Close", false, true, onClose)
		}
	}
}
