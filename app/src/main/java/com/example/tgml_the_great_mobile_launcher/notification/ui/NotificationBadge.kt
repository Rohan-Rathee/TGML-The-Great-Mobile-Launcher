package com.example.tgml_the_great_mobile_launcher.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.tgml_the_great_mobile_launcher.notification.BadgeStyle
import com.example.tgml_the_great_mobile_launcher.notification.NotificationPreferences
import com.example.tgml_the_great_mobile_launcher.notification.NotificationRepository

private val BadgeColor = Color(0xFFE53935)

@Composable
fun BoxScope.NotificationBadgeOverlay(packageName: String) {
	val context = LocalContext.current
	val notifications by NotificationRepository.notifications.collectAsState()
	val version by NotificationPreferences.version.collectAsState()

	val count = remember(notifications, version, packageName) {
		if (!NotificationPreferences.badgesEnabled(context) ||
			!NotificationPreferences.badgeEnabledFor(context, packageName)
		) {
			0
		} else {
			NotificationRepository.badgeCount(packageName, notifications)
		}
	}
	if (count <= 0) return

	val style = remember(version) { NotificationPreferences.badgeStyle(context) }

	when (style) {
		BadgeStyle.DOT -> Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(top = 2.dp, end = 4.dp)
				.size(10.dp)
				.background(BadgeColor, CircleShape)
		)

		BadgeStyle.COUNT -> Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(top = 2.dp, end = 2.dp)
				.defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
				.background(BadgeColor, RoundedCornerShape(9.dp))
				.padding(horizontal = 5.dp, vertical = 1.dp),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = if (count > 99) "99+" else count.toString(),
				color = Color.White,
				fontSize = 10.sp,
				fontWeight = FontWeight.Bold
			)
		}
	}
}
