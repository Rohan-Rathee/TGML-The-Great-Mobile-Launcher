package com.example.tgml_the_great_mobile_launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.tgml_the_great_mobile_launcher.home.HomeEnvironments

@Composable
fun SettingsHomeEnvironments() {
	val context = LocalContext.current
	val version by HomeEnvironments.version.collectAsState()
	val environments = remember(version) { HomeEnvironments.list(context) }
	val activeId = remember(version) { HomeEnvironments.activeId(context) }
	var newName by remember { mutableStateOf("") }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color(0xFF202020))
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Text(
			text = "Each home environment has its own apps, widgets and grid size. The app drawer, notifications and privacy settings are shared.",
			color = Color.LightGray,
			fontSize = 13.sp
		)

		environments.forEach { environment ->
			val isActive = environment.id == activeId
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Column(modifier = Modifier.weight(1f)) {
					Text(environment.name, color = Color.White, fontSize = 15.sp)
					if (isActive) {
						Text("Active", color = Color.Gray, fontSize = 12.sp)
					}
				}
				if (!isActive) {
					TextButton(onClick = { HomeEnvironments.setActive(context, environment.id) }) {
						Text("Switch", color = Color.White)
					}
				}
				if (environment.id != HomeEnvironments.DEFAULT_ID) {
					Spacer(Modifier.width(4.dp))
					TextButton(onClick = { HomeEnvironments.delete(context, environment.id) }) {
						Text("Delete", color = Color.Gray)
					}
				}
			}
		}

		OutlinedTextField(
			value = newName,
			onValueChange = { newName = it },
			label = { Text("New environment name") },
			singleLine = true,
			modifier = Modifier.fillMaxWidth()
		)
		TextButton(onClick = {
			HomeEnvironments.create(context, newName)?.let {
				HomeEnvironments.setActive(context, it.id)
				newName = ""
			}
		}) {
			Text("Create and switch", color = Color.White)
		}
	}
}