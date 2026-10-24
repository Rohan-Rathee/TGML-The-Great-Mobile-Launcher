package com.example.tgml_the_great_mobile_launcher.gestures

object DefaultGestureMappings {
	val mappings = listOf(
		GestureMapping(
			GestureType.SWIPE_UP,
			GestureAction.OPEN_APP_DRAWER,
			GestureContext.HOME
		),
		GestureMapping(
			gesture = GestureType.SWIPE_DOWN,
			action = GestureAction.GO_HOME,
			context = GestureContext.APP_DRAWER
		)
	)
}