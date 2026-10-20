package com.example.tgml_the_great_mobile_launcher.gestures

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

fun Modifier.detectLauncherSwipes(
	edgeOnly: Boolean = false,
	onSwipe: (GestureType) -> Unit
): Modifier = pointerInput(edgeOnly, onSwipe){
	var startX = 0f
	var startY = 0f
	var totalX = 0f
	var totalY = 0f
	var validStart = true

	val threshold = 80.dp.toPx()
	val edge = 32.dp.toPx()

	detectDragGestures(
		onDragStart = { position ->
			startX = position.x
			startY = position.y
			totalX = 0f
			totalY = 0f
			validStart = !edgeOnly ||
					startX <= edge ||
					startX >= size.width - edge ||
					startY <= edge ||
					startY >= size.height - edge
		},
		onDrag = { change, dragAmount ->
			change.consume()
			totalX += dragAmount.x
			totalY += dragAmount.y
		},
		onDragEnd = {
			if (!validStart) return@detectDragGestures

			if (abs(totalX) >= threshold && abs(totalX) > abs(totalY)) {
				onSwipe(
					if (totalX > 0f) GestureType.SWIPE_RIGHT
					else GestureType.SWIPE_LEFT
				)
			} else if (abs(totalY) >= threshold) {
				onSwipe(
					if (totalY > 0f) GestureType.SWIPE_DOWN
					else GestureType.SWIPE_UP
				)
			}
		}
	)
}