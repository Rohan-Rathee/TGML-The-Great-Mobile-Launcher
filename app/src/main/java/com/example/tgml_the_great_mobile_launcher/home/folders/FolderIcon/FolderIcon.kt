
package com.example.tgml_the_great_mobile_launcher.home.folders

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.home.HomeItem
import com.example.tgml_the_great_mobile_launcher.toSafeImageBitmap

@Composable
fun FolderIcon(
	folder: HomeItem,
	contents: List<HomeItem>,
	apps: List<AppInfo>,
	onClick: () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Box(
			modifier = Modifier
				.size(54.dp)
				.background(
					Color.White.copy(alpha = 0.18f),
					RoundedCornerShape(12.dp)
				),
			contentAlignment = Alignment.Center
		) {
			Column(
				verticalArrangement = Arrangement.spacedBy(3.dp)
			) {
				contents.take(4).chunked(2).forEach { rowItems ->
					Row(
						horizontalArrangement = Arrangement.spacedBy(3.dp)
					) {
						rowItems.forEach { item ->
							val app = apps.find {
								it.packageName == item.packageName &&
										it.activityName == item.activityName
							}

							if (app != null) {
								Image(
									bitmap = remember(
										app.packageName,
										app.activityName
									) {
										app.icon.toSafeImageBitmap(48, 48)
									},
									contentDescription = app.name,
									contentScale = ContentScale.Fit,
									modifier = Modifier.size(21.dp)
								)
							} else {
								Box(
									Modifier
										.size(21.dp)
										.background(
											Color.Gray,
											RoundedCornerShape(4.dp)
										)
								)
							}
						}
					}
				}
			}
		}

		Text(
			text = folder.folderName ?: "Folder",
			color = Color.White,
			fontSize = 11.sp,
			maxLines = 1
		)
	}
}
