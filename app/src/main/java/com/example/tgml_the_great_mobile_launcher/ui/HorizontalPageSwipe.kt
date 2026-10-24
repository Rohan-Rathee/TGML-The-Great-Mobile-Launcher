package com.example.tgml_the_great_mobile_launcher.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import kotlin.math.abs

/** Detects horizontal paging without consuming vertical drags used by app layouts. */
fun Modifier.detectHorizontalPageSwipes(onSwipe: (GestureType) -> Unit): Modifier = pointerInput(onSwipe) {
    val edgeWidth = 32.dp.toPx()
    val swipeThreshold = 80.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val startedAtEdge = down.position.x <= edgeWidth || down.position.x >= size.width - edgeWidth ||
            down.position.y <= edgeWidth || down.position.y >= size.height - edgeWidth
	        var total = Offset.Zero
        var horizontal = false
        var tracking = true
        while (tracking) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            val delta = change.position - change.previousPosition
            total += delta
            if (!change.pressed) {
                tracking = false
            } else if (!horizontal && (abs(total.x) > viewConfiguration.touchSlop || abs(total.y) > viewConfiguration.touchSlop)) {
                if (!startedAtEdge && abs(total.x) > abs(total.y)) {
                    horizontal = true
                    change.consume()
                } else if (abs(total.y) >= abs(total.x)) {
                    tracking = false
                }
            } else if (horizontal) {
                change.consume()
            }
        }
        if (horizontal && abs(total.x) >= swipeThreshold) {
            onSwipe(if (total.x < 0f) GestureType.SWIPE_LEFT else GestureType.SWIPE_RIGHT)
        }
    }
}
