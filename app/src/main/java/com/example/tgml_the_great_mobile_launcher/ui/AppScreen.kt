package com.example.tgml_the_great_mobile_launcher.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.LauncherLayout
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import com.example.tgml_the_great_mobile_launcher.gestures.detectLauncherSwipes
import com.example.tgml_the_great_mobile_launcher.layouts.LauncherLayoutView

private const val APP_DRAWER_PAGE_SIZE = 25

@Composable
fun AppScreen(
    apps: List<AppInfo>,
    backgroundUri: Uri?,
    layout: LauncherLayout,
    drawerPage: Int,
    drawerPageCount: Int,
    transitionStyle: PageTransitionStyle,
    onDrawerPageSelected: (Int) -> Unit,
    onAddHomeScreen: () -> Unit,
    onPickBackground: () -> Unit,
    onLayoutChange: (LauncherLayout) -> Unit,
    onTransitionStyleChange: (PageTransitionStyle) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onGesture: (GestureType) -> Unit
) {
    var editMode by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    val launcherScale by animateFloatAsState(
        targetValue = if (editMode) 0.82f else 1f,
        animationSpec = tween(300),
        label = "launcherScale"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { editMode = true })
            }
    ) {
        if (editMode) LauncherBackground(backgroundUri = backgroundUri, blurred = true)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = launcherScale
                    scaleY = launcherScale
                }
	            .then(
					if (layout == LauncherLayout.MULTI_PAGE){
						Modifier.detectHorizontalPageSwipes(onSwipe = onGesture)
					} else Modifier
				)
				.detectLauncherSwipes(edgeOnly = true, onSwipe = onGesture)
        ) {
            LauncherBackground(backgroundUri = backgroundUri)

	        if (layout == LauncherLayout.MULTI_PAGE) {
				val safePage = drawerPage.coerceIn (0, (drawerPageCount - 1).coerceAtLeast(0))

		        AnimatedPageContent(
					page = safePage,
					style = transitionStyle,
					modifier = Modifier.fillMaxSize()
				) {
					targetPage ->
					val pageApps = apps
						.drop(targetPage * APP_DRAWER_PAGE_SIZE)
						.take(APP_DRAWER_PAGE_SIZE)
			        LauncherLayoutView(
						layout = layout,
						apps = pageApps,
						onAppClick = onAppClick
					)
		        }
		        PageIndicator(
					page = safePage,
					pageCount = drawerPageCount,
					onPageSelected = onDrawerPageSelected,
					modifier = Modifier
						.align(Alignment.BottomCenter)
						.padding(bottom = 12.dp)
				)
	        } else {
		        LauncherLayoutView(
					layout = layout,
					apps = apps,
					onAppClick = onAppClick
				)
	        }
        }
        AnimatedVisibility(
            visible = editMode,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
            exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
        ) {
            EditorBar(
                onBackgroundClick = onPickBackground,
                onAddScreen = onAddHomeScreen,
                onSettingsClick = { settingsOpen = true },
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
                onClose = { settingsOpen = false }
            )
        }
    }
}
