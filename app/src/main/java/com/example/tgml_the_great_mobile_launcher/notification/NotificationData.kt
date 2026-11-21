package com.example.tgml_the_great_mobile_launcher.notification

import android.app.Notification
import android.app.PendingIntent
import android.os.Build

data class TgmlNotification(
	val key: String,
	val packageName: String,
	val appName: String,
	val title: String?,
	val text: String?,
	val postTime: Long,
	val contentIntent: PendingIntent?,
	val autoCancel: Boolean,
	val isGroupSummary: Boolean,
	val actions: List<TgmlNotificationAction>,
)

data class TgmlNotificationAction(
	val title: String,
	val action: Notification.Action,
) {
	val isReply: Boolean
		get() = !action.remoteInputs.isNullOrEmpty()

	val isMarkAsRead: Boolean
		get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
				action.semanticAction == Notification.Action.SEMANTIC_ACTION_MARK_AS_READ
}
