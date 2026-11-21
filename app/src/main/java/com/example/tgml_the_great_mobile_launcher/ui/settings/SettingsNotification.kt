package com.example.tgml_the_great_mobile_launcher.ui.settings


import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import com.example.tgml_the_great_mobile_launcher.notification.BadgeStyle
import com.example.tgml_the_great_mobile_launcher.notification.NotificationPreferences
import com.example.tgml_the_great_mobile_launcher.notification.ui.NotificationAccessCard

@Composable
fun SettingsNotifications() {
	val context = LocalContext.current
	val version by NotificationPreferences.version.collectAsState()

	val badgesOn = NotificationPreferences.badgesEnabled(context)
	val style = NotificationPreferences.badgeStyle(context)
	val gesture = NotificationPreferences.notificationGesture(context)
	val apps = remember(version) { launchableApps(context) }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color(0xFF202020))
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		NotificationAccessCard()

		NotifSectionLabel("Badges")
		NotifSwitchRow("Show badges on app icons", badgesOn) {
			NotificationPreferences.setBadgesEnabled(context, it)
		}
		NotifChoiceRow("Dot", style == BadgeStyle.DOT) {
			NotificationPreferences.setBadgeStyle(context, BadgeStyle.DOT)
		}
		NotifChoiceRow("Count", style == BadgeStyle.COUNT) {
			NotificationPreferences.setBadgeStyle(context, BadgeStyle.COUNT)
		}

		NotifSectionLabel("Open notification center with")
		NotifChoiceRow("Off", gesture == null) {
			NotificationPreferences.setNotificationGesture(context, null)
		}
		NotifChoiceRow("Swipe down (replaces the Command Center swipe)", gesture == GestureType.SWIPE_DOWN) {
			NotificationPreferences.setNotificationGesture(context, GestureType.SWIPE_DOWN)
		}

		NotifSectionLabel("Per-app badges")
		apps.forEach { (pkg, label) ->
			NotifSwitchRow(label, NotificationPreferences.badgeEnabledFor(context, pkg)) {
				NotificationPreferences.setBadgeEnabledFor(context, pkg, it)
			}
		}
	}
}
private fun launchableApps(context: Context): List<Pair<String, String>> {
	val pm = context.packageManager
	val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
	return pm.queryIntentActivities(intent, 0)
		.map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
		.distinctBy { it.first }
		.sortedBy { it.second.lowercase() }
}

@Composable
private fun NotifSectionLabel(text: String) {
	Text(
		text = text,
		color = Color.Gray,
		fontSize = 12.sp,
		modifier = Modifier.padding(top = 8.dp)
	)
}
@Composable
private fun NotifSwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
		Spacer(Modifier.width(8.dp))
		Switch(checked = checked, onCheckedChange = onChange)
	}
}
@Composable
private fun NotifChoiceRow(label: String, selected: Boolean, onSelect: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onSelect)
			.padding(vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {

		Text(
			text = if (selected) "●" else "○",
			color = if (selected) Color.White else Color.Gray,
			fontSize = 16.sp,
			fontWeight = FontWeight.Bold
		)
		Spacer(Modifier.width(12.dp))
		Text(label, color = Color.White, fontSize = 15.sp)
	}
}