package com.example.tgml_the_great_mobile_launcher.gestures

object DefaultGestureMappings {
	val mappings = listOf(
		GestureMapping(
			GestureType.SWIPE_UP,
			GestureAction.OPEN_APP_DRAWER,
			GestureContext.HOME
		)
	)
}