package com.example.tgml_the_great_mobile_launcher.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import com.example.tgml_the_great_mobile_launcher.gestures.detectLauncherSwipes

@Composable
fun HomeScreen(
	backgroundUri: Uri?,
	onGesture: (GestureType) -> Unit
){
	Box(
		modifier = Modifier
			.fillMaxSize()
			.detectLauncherSwipes(onSwipe = onGesture)
	){
		LauncherBackground(backgroundUri = backgroundUri)

		Text(
			text = "Swipe up to open apps",
			color = Color.White,
			fontSize = 14.sp,
			modifier = Modifier.align(Alignment.BottomCenter)
		)
	}
}