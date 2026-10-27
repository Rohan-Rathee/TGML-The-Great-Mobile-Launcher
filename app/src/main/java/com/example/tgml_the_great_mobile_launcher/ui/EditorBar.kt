package com.example.tgml_the_great_mobile_launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorBar(onBackgroundClick: () -> Unit, onAddScreen: () -> Unit, onSettingsClick: () -> Unit, onDone: () -> Unit, onAddAppClick: () -> Unit = {})
{
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.padding(horizontal = 12.dp, vertical = 12.dp)
			.background(
				Color.Black.copy(alpha = 0.82f),
				RoundedCornerShape(22.dp)
			)
			.padding(horizontal = 10.dp, vertical = 8.dp)
	) {
		EditorButton(
			icon = "🎨",
			label = "Background",
			onClick = onBackgroundClick
		)
		EditorButton(
			icon = "+",
			label = "Add screen",
			onClick = onAddScreen
		)
		EditorButton(
			icon = "+",
			label = "Add app",
			onClick = onAddAppClick
		)
		EditorButton(
			icon = "⚙",
			label = "Settings",
			onClick = onSettingsClick
		)

		EditorButton(
			icon = "✓",
			label = "Done",
			onClick = onDone
		)
	}
}

@Composable
fun EditorButton(icon: String, label: String, onClick: () -> Unit )
{
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.pointerInput(Unit) {
				detectTapGestures {
					onClick()
				}
			}
			.padding(
				horizontal = 8.dp,
				vertical = 4.dp
			)
	) {

		Text(
			text = icon,
			color = Color.White,
			fontSize = 22.sp
		)
		Text(
			text = label,
			color = Color.White.copy(alpha = 0.75f),
			fontSize = 9.sp
		)
	}
}