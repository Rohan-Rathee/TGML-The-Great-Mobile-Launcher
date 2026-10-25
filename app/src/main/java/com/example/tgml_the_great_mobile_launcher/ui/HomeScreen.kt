package com.example.tgml_the_great_mobile_launcher.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.LauncherLayout
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import com.example.tgml_the_great_mobile_launcher.gestures.detectLauncherSwipes

@Composable
fun HomeScreen(
	backgroundUri: Uri?,
	page: Int,
	pageCount: Int,
	transitionStyle: PageTransitionStyle,
	onPageSelected: (Int) -> Unit,
	onGesture: (GestureType) -> Unit,
	layout: LauncherLayout,
	onAddHomeScreen: () -> Unit,
	onPickBackground: () -> Unit,
	onLayoutChange: (LauncherLayout) -> Unit,
	onTransitionStyleChange: (PageTransitionStyle) -> Unit
) {
	var editMode by remember { mutableStateOf(false) }
	var settingsOpen by remember { mutableStateOf(false) }

	val launcherScale by animateFloatAsState(
		targetValue = if (editMode) 0.88f else 1f,
		animationSpec = tween(300),
		label = "homeLauncherScale"
	)
	Box (
		modifier = Modifier
			.fillMaxSize()
			.pointerInput(Unit) {
				detectTapGestures (
					onLongPress = { editMode = true }
				)
			}
	){
		if (editMode) {
			LauncherBackground(
				backgroundUri = backgroundUri,
				blurred = true
			)
		}
		Box(
	        modifier = Modifier
	            .fillMaxSize()
		        .graphicsLayer {
			        scaleX = launcherScale
			        scaleY = launcherScale
		        }
		        .detectLauncherSwipes(
			        verticalOnly = true,
			        onSwipe = onGesture
		        )
		        .detectHorizontalPageSwipes(onSwipe = onGesture)
		) {
		    AnimatedPageContent(
			    page = page,
			    style = transitionStyle,
			    modifier = Modifier.fillMaxSize()
		    ) { targetPage ->
				Box(Modifier.fillMaxSize()) {
	                LauncherBackground(backgroundUri = backgroundUri)

					Column(
	                    modifier = Modifier
	                        .align(Alignment.BottomCenter)
	                        .padding(bottom = 12.dp),
	                    horizontalAlignment = Alignment.CenterHorizontally
	                ) {
	                    PageIndicator(
	                        page = targetPage,
	                        pageCount = pageCount,
	                        onPageSelected = onPageSelected,
	                        modifier = Modifier.padding(bottom = 8.dp)
	                    )
	                    Text(
	                        text = "Swipe up to open apps",
	                        color = Color.White,
	                        fontSize = 14.sp
	                    )
	                }
	            }
	        }
		    if (!editMode){
				Text(
					text = "Edit",
					color = Color.White,
					fontSize = 13.sp,
					modifier = Modifier
						.align ( Alignment.BottomEnd )
						.padding(end = 18.dp, bottom = 22.dp)
						.background(
							Color.Black.copy(alpha = 0.65f),
							RoundedCornerShape(18.dp)
						)
						.clickable { editMode = true}
						.padding(horizontal = 14.dp, vertical = 8.dp)
				)
		    }
	    }
		AnimatedVisibility(
			visible = editMode,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.navigationBarsPadding(),
			enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
			exit = fadeOut(tween(2000)) + slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),

		){
			EditorBar(
				onBackgroundClick = onPickBackground,
				onAddScreen = onAddHomeScreen,
				onSettingsClick = { settingsOpen = true},
				onDone = { editMode = false }
			)
		}
		AnimatedVisibility(
			visible = settingsOpen,
			enter = fadeIn(tween(200)),
			exit = fadeOut(tween(200))
		) {
			LauncherSettings(
				currentLayout = layout,
				onLayoutChange = onLayoutChange,
				currentTransitionStyle = transitionStyle,
				onTransitionStyleChange = onTransitionStyleChange,
				onClose = { settingsOpen = false}
			)
		}
	}
}
