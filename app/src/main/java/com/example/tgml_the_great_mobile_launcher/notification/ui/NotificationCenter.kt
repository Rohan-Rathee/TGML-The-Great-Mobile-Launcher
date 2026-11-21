package com.example.tgml_the_great_mobile_launcher.notification.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.tgml_the_great_mobile_launcher.notification.NotificationRepository
import com.example.tgml_the_great_mobile_launcher.notification.TgmlNotification
import com.example.tgml_the_great_mobile_launcher.notification.TgmlNotificationAction
import com.example.tgml_the_great_mobile_launcher.notification.TgmlNotificationActions
import com.example.tgml_the_great_mobile_launcher.notification.TgmlNotificationListener
import com.example.tgml_the_great_mobile_launcher.toSafeImageBitmap

private data class NotificationGroup(
	val packageName: String,
	val appName: String,
	val items: List<TgmlNotification>,
)

/** Full-screen list of current notifications, grouped by app. */
@Composable
fun NotificationCenter(onClose: () -> Unit) {
	val context = LocalContext.current
	val notifications by NotificationRepository.notifications.collectAsState()
	val accessGranted = rememberNotificationAccess()

	val groups = remember(notifications) {
		notifications
			.groupBy { it.packageName }
			.map { (pkg, list) -> NotificationGroup(pkg, list.first().appName, list) }
			.sortedByDescending { it.items.first().postTime }
	}
	val expanded = remember { mutableStateMapOf<String, Boolean>() }

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xE6101010))
			.statusBarsPadding()
	) {
		Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "Notifications",
					color = Color.White,
					fontSize = 22.sp,
					fontWeight = FontWeight.SemiBold,
					modifier = Modifier.weight(1f)
				)
				if (accessGranted && notifications.isNotEmpty()) {
					TextButton(onClick = {
						TgmlNotificationListener.dismissAll(notifications.map { it.key })
					}) {
						Text("Clear all", color = Color.White)
					}
				}
				TextButton(onClick = onClose) {
					Text("Close", color = Color.White)
				}
			}
			Spacer(Modifier.height(8.dp))

			when {
				!accessGranted -> NotificationAccessCard()
				groups.isEmpty() -> Text(
					text = "No notifications",
					color = Color.Gray,
					modifier = Modifier.padding(top = 24.dp)
				)
				else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
					groups.forEach { group ->
						val isExpanded = expanded[group.packageName] == true
						val visible = if (isExpanded) group.items else group.items.take(1)

						item(key = "group-${group.packageName}") {
							GroupHeader(
								group = group,
								expanded = isExpanded,
								onToggle = { expanded[group.packageName] = !isExpanded }
							)
						}
						items(visible, key = { it.key }) { notification ->
							NotificationCard(
								notification = notification,
								onOpen = {
									TgmlNotificationActions.open(context, notification)
									onClose()
								},
								onDismiss = { TgmlNotificationListener.dismiss(notification.key) }
							)
						}
					}
				}
			}
		}
	}
}

/** Single notification row used by the Command Center search results. */
@Composable
fun NotificationSearchRow(notification: TgmlNotification, onClick: () -> Unit) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(vertical = 7.dp)
	) {
		Text(notification.appName, color = Color.Gray, fontSize = 11.sp)
		Text(
			text = notification.title ?: notification.appName,
			color = Color.White,
			fontSize = 14.sp,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

/** Explains and links to the system screen where the user grants notification access. */
@Composable
fun NotificationAccessCard() {
	val context = LocalContext.current
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(14.dp))
			.background(Color(0xFF202020))
			.padding(14.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			text = "Allow notification access",
			color = Color.White,
			fontSize = 15.sp,
			fontWeight = FontWeight.SemiBold
		)
		Text(
			text = "TGML reads your notifications to show them here, add badges and send quick replies. Android asks you to turn this on for TGML in its settings.",
			color = Color.LightGray,
			fontSize = 13.sp
		)
		TextButton(onClick = {
			context.startActivity(
				Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			)
		}) {
			Text("Open notification settings", color = Color.White)
		}
	}
}

@Composable
private fun GroupHeader(group: NotificationGroup, expanded: Boolean, onToggle: () -> Unit) {
	val context = LocalContext.current
	val icon = remember(group.packageName) { appIcon(context, group.packageName) }
	val canToggle = group.items.size > 1

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(Color(0xFF181818))
			.clickable(enabled = canToggle, onClick = onToggle)
			.padding(horizontal = 12.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		icon?.let {
			Image(
				bitmap = it,
				contentDescription = group.appName,
				modifier = Modifier.size(24.dp)
			)
			Spacer(Modifier.width(10.dp))
		}
		Text(
			text = group.appName,
			color = Color.White,
			fontSize = 14.sp,
			fontWeight = FontWeight.SemiBold,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f)
		)
		Text(group.items.size.toString(), color = Color.Gray, fontSize = 12.sp)
		if (canToggle) {
			Spacer(Modifier.width(8.dp))
			Text(if (expanded) "▴" else "▾", color = Color.Gray, fontSize = 14.sp)
		}
	}
}

@Composable
private fun NotificationCard(
	notification: TgmlNotification,
	onOpen: () -> Unit,
	onDismiss: () -> Unit,
) {
	val context = LocalContext.current
	var replyingTo by remember(notification.key) { mutableStateOf<TgmlNotificationAction?>(null) }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(14.dp))
			.background(Color(0xFF202020))
			.clickable(onClick = onOpen)
			.padding(12.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(
				text = relativeTime(notification.postTime),
				color = Color.Gray,
				fontSize = 11.sp,
				modifier = Modifier.weight(1f)
			)
			Text(
				text = "✕",
				color = Color.Gray,
				fontSize = 14.sp,
				modifier = Modifier
					.clickable(onClick = onDismiss)
					.padding(horizontal = 4.dp)
			)
		}

		Text(
			text = notification.title ?: notification.appName,
			color = Color.White,
			fontSize = 14.sp,
			fontWeight = FontWeight.SemiBold,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis
		)
		notification.text?.let {
			Text(
				text = it,
				color = Color.LightGray,
				fontSize = 13.sp,
				maxLines = 3,
				overflow = TextOverflow.Ellipsis
			)
		}

		if (notification.actions.isNotEmpty()) {
			Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
				notification.actions.forEach { action ->
					TextButton(
						onClick = {
							if (action.isReply) {
								replyingTo = if (replyingTo == action) null else action
							} else {
								TgmlNotificationActions.trigger(action)
							}
						},
						colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
					) {
						Text(action.title, fontSize = 12.sp)
					}
				}
			}
		}

		replyingTo?.let { action ->
			ReplyField(
				action = action,
				onSend = { text ->
					if (TgmlNotificationActions.reply(context, action, text)) replyingTo = null
				},
				onCancel = { replyingTo = null }
			)
		}
	}
}

@Composable
private fun ReplyField(
	action: TgmlNotificationAction,
	onSend: (String) -> Unit,
	onCancel: () -> Unit,
) {
	var text by remember(action) { mutableStateOf("") }

	Row(verticalAlignment = Alignment.CenterVertically) {
		OutlinedTextField(
			value = text,
			onValueChange = { text = it },
			placeholder = { Text(action.title) },
			singleLine = true,
			modifier = Modifier.weight(1f)
		)
		TextButton(onClick = onCancel) {
			Text("Cancel", color = Color.Gray)
		}
		TextButton(onClick = { if (text.isNotBlank()) onSend(text) }) {
			Text("Send", color = Color.White)
		}
	}
}

/** Re-checks the listener permission whenever the screen comes back to the foreground. */
@Composable
private fun rememberNotificationAccess(): Boolean {
	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current
	var granted by remember { mutableStateOf(isAccessGranted(context)) }

	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) granted = isAccessGranted(context)
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
	}
	return granted
}

private fun isAccessGranted(context: Context): Boolean =
	NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

private fun relativeTime(time: Long): String =
	DateUtils.getRelativeTimeSpanString(
		time,
		System.currentTimeMillis(),
		DateUtils.MINUTE_IN_MILLIS,
		DateUtils.FORMAT_ABBREV_RELATIVE
	).toString()

private fun appIcon(context: Context, packageName: String): ImageBitmap? = try {
	context.packageManager.getApplicationIcon(packageName).toSafeImageBitmap(64, 64)
} catch (_: Exception) {
	null
}
