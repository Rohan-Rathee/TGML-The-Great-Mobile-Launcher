package com.example.tgml_the_great_mobile_launcher.notification

import android.content.Context
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class BadgeStyle { DOT, COUNT }

object NotificationPreferences {
	private const val PREFS = "launcher_notifications"
	private const val KEY_BADGES = "badges_enabled"
	private const val KEY_STYLE = "badge_style"
	private const val KEY_GESTURE = "notification_gesture"
	private const val KEY_APP_OFF = "badge_off:"

	private val changes = MutableStateFlow(0)
	val version: StateFlow<Int> = changes.asStateFlow()
	private fun prefs(context: Context) =
		context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
	private fun bump() {
		changes.value = changes.value + 1
	}
	fun badgesEnabled(context: Context): Boolean =
		prefs(context).getBoolean(KEY_BADGES, true)

	fun setBadgesEnabled(context: Context, enabled: Boolean) {
		prefs(context).edit().putBoolean(KEY_BADGES, enabled).apply()
		bump()
	}
	fun badgeStyle(context: Context): BadgeStyle =
		prefs(context).getString(KEY_STYLE, null)
			?.let { runCatching { BadgeStyle.valueOf(it) }.getOrNull() }
			?: BadgeStyle.COUNT
	fun setBadgeStyle(context: Context, style: BadgeStyle) {
		prefs(context).edit().putString(KEY_STYLE, style.name).apply()
		bump()
	}
	fun badgeEnabledFor(context: Context, packageName: String): Boolean =
		!prefs(context).getBoolean(KEY_APP_OFF + packageName, false)

	fun setBadgeEnabledFor(context: Context, packageName: String, enabled: Boolean) {
		val editor = prefs(context).edit()
		if (enabled) editor.remove(KEY_APP_OFF + packageName)
		else editor.putBoolean(KEY_APP_OFF + packageName, true)
		editor.apply()
		bump()
	}
	fun notificationGesture(context: Context): GestureType? =
		prefs(context).getString(KEY_GESTURE, null)
			?.let { runCatching { GestureType.valueOf(it) }.getOrNull() }

	fun setNotificationGesture(context: Context, gesture: GestureType?) {
		val editor = prefs(context).edit()
		if (gesture == null) editor.remove(KEY_GESTURE)
		else editor.putString(KEY_GESTURE, gesture.name)
		editor.apply()
		bump()
	}
}
