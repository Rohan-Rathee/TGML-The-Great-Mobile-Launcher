package com.example.tgml_the_great_mobile_launcher.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class TgmlNotificationListener : NotificationListenerService() {

	override fun onListenerConnected() {
		super.onListenerConnected()
		instance = this
		NotificationRepository.replaceAll(
			activeNotifications?.mapNotNull { toModel(it) }.orEmpty()
		)
	}
	override fun onListenerDisconnected() {
		instance = null
		NotificationRepository.clear()
		super.onListenerDisconnected()
	}
	override fun onNotificationPosted(sbn: StatusBarNotification) {
		val item = toModel(sbn) ?: return
		NotificationRepository.upsert(item)
	}
	override fun onNotificationRemoved(sbn: StatusBarNotification) {
		NotificationRepository.remove(sbn.key)
	}
	private fun toModel(sbn: StatusBarNotification): TgmlNotification? {
		if (sbn.packageName == packageName) return null
		val notification = sbn.notification ?: return null
		if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) return null
		val extras = notification.extras
		val title = extras.getCharSequence(Notification.EXTRA_TITLE)
			?.toString()?.takeIf { it.isNotBlank() }
		val text = (extras.getCharSequence(Notification.EXTRA_TEXT)
			?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
			?.toString()?.takeIf { it.isNotBlank() }
		val actions = (notification.actions ?: emptyArray()).mapNotNull { action ->
			action.title?.toString()?.takeIf { it.isNotBlank() }?.let {
				TgmlNotificationAction(it, action)
			}
		}
		return TgmlNotification(
			key = sbn.key,
			packageName = sbn.packageName,
			appName = labelFor(sbn.packageName),
			title = title,
			text = text,
			postTime = sbn.postTime,
			contentIntent = notification.contentIntent,
			autoCancel = (notification.flags and Notification.FLAG_AUTO_CANCEL) != 0,
			isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0,
			actions = actions,
		)
	}
	private fun labelFor(pkg: String): String = try {
		packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
	} catch (_: Exception) {
		pkg
	}
	companion object {
		@Volatile
		private var instance: TgmlNotificationListener? = null

		val isConnected: Boolean
			get() = instance != null
		fun dismiss(key: String) {
			instance?.cancelNotification(key)
			NotificationRepository.remove(key)
		}

		fun dismissAll(keys: List<String>) {
			keys.forEach { dismiss(it) }
		}
	}
}
