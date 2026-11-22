package com.example.tgml_the_great_mobile_launcher.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.privacy.AppPrivacyPreferences

@Composable
fun SettingsAppLock() {
	val context = LocalContext.current
	// Reading the version makes this screen recompose after every lock or hide change.
	val version by AppPrivacyPreferences.version.collectAsState()
	val apps = remember(version) { allLaunchableApps(context) }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color(0xFF202020))
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Text(
			text = "Locked apps ask for your fingerprint or screen lock before they open. Hidden apps are removed from the app drawer, home screen, Command Center and side handle.",
			color = Color.LightGray,
			fontSize = 13.sp
		)

		apps.forEach { (pkg, label) ->
			Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Text(label, color = Color.White, fontSize = 15.sp)
				Row(verticalAlignment = Alignment.CenterVertically) {
					PrivacyToggle(
						label = "Lock",
						checked = AppPrivacyPreferences.isLocked(context, pkg),
						onChange = { AppPrivacyPreferences.setLocked(context, pkg, it) }
					)
					Spacer(Modifier.width(24.dp))
					PrivacyToggle(
						label = "Hide",
						checked = AppPrivacyPreferences.isHidden(context, pkg),
						onChange = { AppPrivacyPreferences.setHidden(context, pkg, it) }
					)
				}
			}
		}
	}
}

@Composable
private fun PrivacyToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
	Row(verticalAlignment = Alignment.CenterVertically) {
		Switch(checked = checked, onCheckedChange = onChange)
		Spacer(Modifier.width(8.dp))
		Text(label, color = Color.White, fontSize = 13.sp)
	}
}

/** Every launchable app, including hidden ones, so the user can manage them here. */
private fun allLaunchableApps(context: Context): List<Pair<String, String>> {
	val pm = context.packageManager
	val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
	return pm.queryIntentActivities(intent, 0)
		.map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
		.distinctBy { it.first }
		.sortedBy { it.second.lowercase() }
}
