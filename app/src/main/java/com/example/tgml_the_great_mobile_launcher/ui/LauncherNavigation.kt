package com.example.tgml_the_great_mobile_launcher.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.LauncherLayout
import com.example.tgml_the_great_mobile_launcher.gestures.GestureAction
import com.example.tgml_the_great_mobile_launcher.gestures.GestureContext
import com.example.tgml_the_great_mobile_launcher.gestures.GestureHandler
import com.example.tgml_the_great_mobile_launcher.gestures.GestureMapping
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType

@Composable
fun LauncherNavigation(
	apps: List<AppInfo>,
	backgroundUri: Uri?,
	layout: LauncherLayout,
	gestureMappings: List<GestureMapping>,
	onPickBackground: () -> Unit,
	onLayoutChange: (LauncherLayout) -> Unit,
	onAppClick: (AppInfo) -> Unit,
){
	var drawerOpen by rememberSaveable{ mutableStateOf(false) }
	var openingGesture by rememberSaveable{ mutableStateOf<GestureType?>(null)}
	val handler = remember(gestureMappings) { GestureHandler(gestureMappings) }

	fun reverseOf(gesture: GestureType): GestureType? = when (gesture) {
		GestureType.SWIPE_UP -> GestureType.SWIPE_DOWN
		GestureType.SWIPE_DOWN -> GestureType.SWIPE_UP
		GestureType.SWIPE_LEFT -> GestureType.SWIPE_RIGHT
		GestureType.SWIPE_RIGHT -> GestureType.SWIPE_LEFT
		else -> null
	}

	fun handleGesture(gesture:GestureType){
		val context = if (drawerOpen) GestureContext.APP_DRAWER else GestureContext.HOME
		val action = handler.resolve(gesture, context)

		when {
			drawerOpen && (action == GestureAction.GO_HOME || (action == null && openingGesture?.let {
				reverseOf(
					it
				) == gesture
			} == true)) -> {
				drawerOpen = false
				openingGesture = null
			}
			action == GestureAction.OPEN_APP_DRAWER && !drawerOpen -> {
				drawerOpen = true
				openingGesture = gesture
			}
			action == GestureAction.CHANGE_LAYOUT -> {
				val layouts = LauncherLayout.entries
				val next = (layouts.indexOf(layout) + 1) % layouts.size
				onLayoutChange(layouts[next])
			}
			else -> Unit
		}
	}

	if (drawerOpen) {
		AppScreen(
			apps = apps,
			backgroundUri = backgroundUri,
			layout = layout,
			onPickBackground = onPickBackground,
			onLayoutChange = onLayoutChange,
			onAppClick = onAppClick,
			onGesture = ::handleGesture
		)
	} else {
		HomeScreen(
			backgroundUri = backgroundUri,
			onGesture = ::handleGesture
		)
	}
}