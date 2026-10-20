package com.example.tgml_the_great_mobile_launcher.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.toSafeImageBitmap

@Composable
fun GridScroll(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit)
{
	LazyVerticalGrid(
		columns = GridCells.Fixed(5),
		modifier = Modifier
			.fillMaxSize()
			.padding(
				start = 40.dp,
				end = 40.dp,
				top = 40.dp,
				bottom = 40.dp
			),
		verticalArrangement = Arrangement.spacedBy(20.dp),
		horizontalArrangement = Arrangement.spacedBy(20.dp)
	) {
		items(apps) { app ->
			GridAppIcon(
				app = app,
				onClick = { onAppClick(app) }
			)
		}
	}
}

@Composable
fun GridAppIcon(app: AppInfo, onClick: () -> Unit)
{
	val bitmap = remember(key1 = app.packageName, key2 = app.activityName) {
		app.icon.toSafeImageBitmap(width = 128, height = 128)
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.clickable {
				onClick()
			}
			.padding(8.dp)
	) {
		Image(
			bitmap = bitmap,
			contentDescription = app.name,
			modifier = Modifier.size(64.dp)
		)

		Text(
			text = if (app.name.length > 10) {
				app.name.take(7) + "..."
			} else {
				app.name
			},
			color = Color.White,
			maxLines = 1,
			fontSize = 12.sp
		)
	}
}