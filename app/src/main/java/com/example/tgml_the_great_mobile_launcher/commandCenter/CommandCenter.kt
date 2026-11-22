package com.example.tgml_the_great_mobile_launcher.commandcenter

import androidx.compose.runtime.collectAsState
import com.example.tgml_the_great_mobile_launcher.notification.NotificationRepository
import com.example.tgml_the_great_mobile_launcher.notification.TgmlNotificationActions
import com.example.tgml_the_great_mobile_launcher.notification.ui.NotificationSearchRow

import android.content.Intent
import android.net.Uri

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.home.HomeItem
import com.example.tgml_the_great_mobile_launcher.home.HomeItemType
import com.example.tgml_the_great_mobile_launcher.toSafeImageBitmap

@Composable
fun CommandCenter(
	apps: List<AppInfo>,
	homeItems: List<HomeItem>,
	onLaunchApp: (AppInfo) -> Unit,
	onPinApp: (AppInfo) -> Unit,
	onOpenSettings: () -> Unit,
	onClose: () -> Unit
) {
	val context = LocalContext.current
	var query by remember { mutableStateOf("") }
	var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
	val normalized = query.trim().lowercase()
	val matchingApps = remember(normalized, apps) {
		if (normalized.isBlank()) emptyList()
		else apps.filter {
			it.name.lowercase().contains(normalized) ||
					it.packageName.lowercase().contains(normalized)
		}.take(30)
	}
	val matchingFolders = remember(normalized, homeItems) {
		if (normalized.isBlank()) emptyList()
		else homeItems.filter {
			it.type == HomeItemType.FOLDER &&
					it.folderName.orEmpty().lowercase().contains(normalized)
		}
	}
	val recent = remember(apps) { AppUsageStore.recent(context, apps) }
	val frequent = remember(apps) { AppUsageStore.frequent(context, apps) }
	val notifications by NotificationRepository.notifications.collectAsState()
	val matchingNotifications = remember(normalized, notifications) {
		if (normalized.isBlank()) emptyList()
		else notifications.filter {
			it.appName.lowercase().contains(normalized) ||
				it.title.orEmpty().lowercase().contains(normalized) ||
				it.text.orEmpty().lowercase().contains(normalized)
		}.take(10)
	}

	AlertDialog(
		onDismissRequest = onClose,
		containerColor = Color(0xFF171717),
		title = {
			Text("Command Center", color = Color.White, fontSize = 22.sp)
		},
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
				OutlinedTextField(
					value = query,
					onValueChange = { query = it },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					placeholder = { Text("Search apps, folders, settings") },
					shape = RoundedCornerShape(16.dp)
				)

				if (normalized.isBlank()) {
					if (recent.isNotEmpty()) {
						SectionTitle("Recently used")
						recent.forEach { app ->
							AppResultRow(app = app, onClick = { onLaunchApp(app) })
						}
					}
					if (frequent.isNotEmpty()) {
						SectionTitle("Frequently used")
						frequent.filterNot { app -> recent.any { it.packageName == app.packageName && it.activityName == app.activityName } }
							.take(5)
							.forEach { app ->
								AppResultRow(app = app, onClick = { onLaunchApp(app) })
							}
					}
					SectionTitle("Quick access")
					Text(
						"Launcher settings",
						color = Color.White,
						modifier = Modifier
							.fillMaxWidth()
							.clickable(onClick = onOpenSettings)
							.padding(vertical = 10.dp)
					)
				} else {
					LazyColumn(
						modifier = Modifier.height(360.dp),
						verticalArrangement = Arrangement.spacedBy(4.dp)
					) {
						if (matchingApps.isNotEmpty()) {
							item { SectionTitle("Apps") }
							items(matchingApps, key = { it.packageName + it.activityName }) { app ->
								AppResultRow(app = app, onClick = { selectedApp = app })
							}
						}
						if (matchingNotifications.isNotEmpty()) {
							item { SectionTitle("Notifications") }
							items(matchingNotifications, key = { it.key }) { notification ->
								NotificationSearchRow(notification) {
									TgmlNotificationActions.open(context, notification)
									onClose()
								}
							}
						}
						if (matchingFolders.isNotEmpty()) {
							item { SectionTitle("Home screen folders") }
							items(matchingFolders, key = { it.id }) { folder ->
								Text(
									folder.folderName ?: "Folder",
									color = Color.White,
									modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
								)
							}
						}
						if (matchingApps.isEmpty() && matchingFolders.isEmpty() && matchingNotifications.isEmpty()) {
							item {
								Text("No results found", color = Color.Gray, modifier = Modifier.padding(12.dp))
							}
						}
						item {
							Text(
								"Settings",
								color = Color.LightGray,
								fontSize = 13.sp,
								modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
							)
							listOf("Launcher settings", "Wallpaper", "Layout", "Page transitions", "Side Handle")
								.filter { it.lowercase().contains(normalized) }
								.forEach { label ->
									Text(
										label,
										color = Color.White,
										modifier = Modifier
											.fillMaxWidth()
											.clickable(onClick = onOpenSettings)
											.padding(vertical = 9.dp)
									)
								}
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onClose) { Text("Close") }
		}
	)

	selectedApp?.let { app ->
		AlertDialog(
			onDismissRequest = { selectedApp = null },
			containerColor = Color(0xFF202020),
			title = { Text(app.name, color = Color.White) },
			text = {
				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					ActionText("Open", onClick = { selectedApp = null; onLaunchApp(app) })
					ActionText("App info", onClick = {
						selectedApp = null
						context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
							data = Uri.parse("package:${app.packageName}")
						})
					})
					ActionText("Uninstall", onClick = {
						selectedApp = null
						context.startActivity(
							Intent(
								Intent.ACTION_DELETE,
								Uri.parse("package:${app.packageName}")
							)
						)
					})
					ActionText("Add to home screen", onClick = { selectedApp = null; onPinApp(app) })
				}
			},
			confirmButton = {
				TextButton(onClick = { selectedApp = null }) { Text("Done") }
			}
		)
	}
}

@Composable
private fun AppResultRow(app: AppInfo, onClick: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(vertical = 7.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Image(
			bitmap = app.icon.toSafeImageBitmap(),
			contentDescription = app.name,
			modifier = Modifier.size(34.dp)
		)
		Spacer(Modifier.width(12.dp))
		Text(app.name, color = Color.White, fontSize = 15.sp)
	}
}

@Composable
private fun SectionTitle(value: String) {
	Text(value, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun ActionText(value: String, onClick: () -> Unit) {
	Text(
		value,
		color = Color.White,
		modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 9.dp)
	)
}
