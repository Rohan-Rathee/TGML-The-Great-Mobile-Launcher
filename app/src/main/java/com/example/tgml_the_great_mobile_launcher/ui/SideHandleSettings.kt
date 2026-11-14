package com.example.tgml_the_great_mobile_launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.SideHandleFolder
import java.util.UUID

@Composable
fun SideHandleSettings(
	apps: List<AppInfo>,
	enabled: Boolean,
	homeIntegration: Boolean,
	folders: List<SideHandleFolder>,
	onEnabledChange: (Boolean) -> Unit,
	onHomeIntegrationChange: (Boolean) -> Unit,
	onFoldersChange: (List<SideHandleFolder>) -> Unit
) {
	var editingFolder by remember { mutableStateOf<SideHandleFolder?>(null) }
	var folderName by remember { mutableStateOf("") }

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text("Side Handle", color = Color.White, fontSize = 18.sp)

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { onEnabledChange(!enabled) }
				.padding(vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Checkbox(
				checked = enabled,
				onCheckedChange = null
			)
			Text(
				text = "Enable side handle",
				color = Color.White,
				fontSize = 14.sp,
				modifier = Modifier.weight(1f).padding(start = 8.dp)
			)
		}

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clickable(enabled = enabled) { onHomeIntegrationChange(!homeIntegration) }
				.padding(vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Checkbox(
				checked = homeIntegration,
				enabled = enabled,
				onCheckedChange = null
			)
			Text(
				text = "Expand folders on TGML home screen",
				color = if (enabled) Color.White else Color.White.copy(alpha = 0.45f),
				fontSize = 14.sp,
				modifier = Modifier.weight(1f).padding(start = 8.dp)
			)
		}

		Text("Folders", color = Color.White, fontSize = 16.sp)

		folders.forEachIndexed { index, folder ->
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = folder.name,
					color = Color.White,
					modifier = Modifier.weight(1f)
				)
				TextButton(onClick = {
					editingFolder = folder
					folderName = folder.name
				}) { Text("Edit") }
				TextButton(
					enabled = index > 0,
					onClick = {
						val updated = folders.toMutableList()
						val item = updated.removeAt(index)
						updated.add(index - 1, item)
						onFoldersChange(updated)
					}
				) { Text("↑") }
				TextButton(
					enabled = index < folders.lastIndex,
					onClick = {
						val updated = folders.toMutableList()
						val item = updated.removeAt(index)
						updated.add(index + 1, item)
						onFoldersChange(updated)
					}
				) { Text("↓") }
				TextButton(onClick = {
					onFoldersChange(folders.filterNot { it.id == folder.id })
				}) { Text("Delete") }
			}
		}

		TextButton(onClick = {
			editingFolder = SideHandleFolder(UUID.randomUUID().toString(), "", emptyList())
			folderName = ""
		}) {
			Text("Add folder")
		}
	}

	editingFolder?.let { folder ->
		var selectedPackages by remember(folder.id) { mutableStateOf(folder.packages.toSet()) }

		AlertDialog(
			onDismissRequest = { editingFolder = null },
			title = { Text("Edit folder", color = Color.White) },
			text = {
				Column {
					OutlinedTextField(
						value = folderName,
						onValueChange = { folderName = it },
						label = { Text("Folder name") },
						singleLine = true
					)
					Text(
						text = "Select apps",
						color = Color.White,
						modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
					)
					LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
						items(apps, key = { it.packageName }) { app ->
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.clickable {
										selectedPackages = if (app.packageName in selectedPackages) selectedPackages - app.packageName else selectedPackages + app.packageName
									}
									.padding(vertical = 2.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								Checkbox(
									checked = app.packageName in selectedPackages,
									onCheckedChange = null
								)
								Text(
									text = app.name,
									color = Color.White,
									fontSize = 14.sp,
									modifier = Modifier.weight(1f).padding(start = 8.dp)
								)
							}
						}
					}
					}
			},
			confirmButton = {
				TextButton(onClick = {
					val updatedFolder = folder.copy(
						name = folderName.trim().ifBlank { "Folder" },
						packages = apps.filter { it.packageName in selectedPackages }.map { it.packageName }
					)
					val updated = folders.toMutableList()
					val index = updated.indexOfFirst { it.id == folder.id }
					if (index >= 0) updated[index] = updatedFolder else updated.add(updatedFolder)
					onFoldersChange(updated)
					editingFolder = null
				}) { Text("Save") }
			},
			dismissButton = {
				TextButton(onClick = { editingFolder = null }) { Text("Cancel") }
			}
		)
	}
}