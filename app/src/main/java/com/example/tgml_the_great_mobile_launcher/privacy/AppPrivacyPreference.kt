package com.example.tgml_the_great_mobile_launcher.privacy

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppPrivacyPreferences {
	private const val PREFS = "launcher_privacy"
	private const val KEY_LOCK = "locked:"
	private const val KEY_HIDE = "hidden:"

	private val changes = MutableStateFlow(0)
	val version: StateFlow<Int> = changes.asStateFlow()

	private fun prefs(context: Context) =
		context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

	fun isLocked(context: Context, packageName: String): Boolean =
		prefs(context).getBoolean(KEY_LOCK + packageName, false)

	fun setLocked(context: Context, packageName: String, locked: Boolean) {
		prefs(context).edit().apply {
			if (locked) putBoolean(KEY_LOCK + packageName, true) else remove(KEY_LOCK + packageName)
		}.apply()
		changes.value = changes.value + 1
	}
	fun isHidden(context: Context, packageName: String): Boolean =
		prefs(context).getBoolean(KEY_HIDE + packageName, false)
	fun setHidden(context: Context, packageName: String, hidden: Boolean) {
		prefs(context).edit().apply {
			if (hidden) putBoolean(KEY_HIDE + packageName, true) else remove(KEY_HIDE + packageName)
		}.apply()
		changes.value = changes.value + 1
	}
}
