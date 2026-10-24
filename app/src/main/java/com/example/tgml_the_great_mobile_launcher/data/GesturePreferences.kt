package com.example.tgml_the_great_mobile_launcher.data

import android.content.Context
import com.example.tgml_the_great_mobile_launcher.gestures.DefaultGestureMappings
import com.example.tgml_the_great_mobile_launcher.gestures.GestureAction
import com.example.tgml_the_great_mobile_launcher.gestures.GestureContext
import com.example.tgml_the_great_mobile_launcher.gestures.GestureMapping
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType

object GesturePreferences {
	private const val PREFS_NAME = "launcher_gestures"
	private const val PREFIX = "mapping:"

	fun load(context: Context): List<GestureMapping> {
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		val entries = prefs.all.filterKeys { it.startsWith(PREFIX) }

		val savedMappings = entries.mapNotNull { (key, value) ->
			val parts = key.removePrefix(PREFIX).split(":")
			if (parts.size != 2 || value !is String) return@mapNotNull null

			try {
				val gestureContext = GestureContext.valueOf(parts[0])
				val gestureType = GestureType.valueOf(parts[1])
				val action = GestureAction.valueOf(value)
				GestureMapping(gestureType, action, gestureContext)
			} catch (_: IllegalArgumentException) {
				null
			}
		}

		val missingDefaults = DefaultGestureMappings.mappings.filterNot { default ->
			savedMappings.any { saved ->
				saved.context == default.context && saved.gesture == default.gesture
			}
		}
		return missingDefaults + savedMappings
	}

	fun save(context: Context, mappings: List<GestureMapping>) {
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		prefs.edit().clear().apply {
			mappings.forEach { item ->
				putString(
					"$PREFIX${item.context.name}:${item.gesture.name}",
					item.action.name
				)
			}
		}.apply()
	}

	fun reset(context: Context) {
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.edit()
			.clear()
			.apply()
	}
}