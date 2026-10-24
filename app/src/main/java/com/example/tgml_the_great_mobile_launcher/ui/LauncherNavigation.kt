package com.example.tgml_the_great_mobile_launcher.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

private const val APP_DRAWER_PAGE_SIZE = 25

@Composable
fun LauncherNavigation(
    apps: List<AppInfo>,
    backgroundUri: Uri?,
    layout: LauncherLayout,
    gestureMappings: List<GestureMapping>,
    initialHomePageCount: Int,
    transitionStyle: PageTransitionStyle,
    onHomePageCountChange: (Int) -> Unit,
    onTransitionStyleChange: (PageTransitionStyle) -> Unit,
    onPickBackground: () -> Unit,
    onLayoutChange: (LauncherLayout) -> Unit,
    onAppClick: (AppInfo) -> Unit
) {
    var drawerOpen by rememberSaveable { mutableStateOf(false) }
    var openingGesture by rememberSaveable { mutableStateOf<GestureType?>(null) }
    var homePage by rememberSaveable { mutableStateOf(0) }
    var homePageCount by rememberSaveable { mutableStateOf(initialHomePageCount.coerceAtLeast(1)) }
    var drawerPage by rememberSaveable { mutableStateOf(0) }
    val drawerPageCount = maxOf(1, (apps.size + APP_DRAWER_PAGE_SIZE - 1) / APP_DRAWER_PAGE_SIZE)
    val handler = remember(gestureMappings) { GestureHandler(gestureMappings) }

    LaunchedEffect(initialHomePageCount) {
        homePageCount = initialHomePageCount.coerceAtLeast(1)
        homePage = homePage.coerceIn(0, homePageCount - 1)
    }
    LaunchedEffect(drawerPageCount) {
        drawerPage = drawerPage.coerceIn(0, drawerPageCount - 1)
    }
    BackHandler(enabled = drawerOpen) {
        drawerOpen = false
        openingGesture = null
    }

    fun reverseOf(gesture: GestureType): GestureType? = when (gesture) {
        GestureType.SWIPE_UP -> GestureType.SWIPE_DOWN
        GestureType.SWIPE_DOWN -> GestureType.SWIPE_UP
        GestureType.SWIPE_LEFT -> GestureType.SWIPE_RIGHT
        GestureType.SWIPE_RIGHT -> GestureType.SWIPE_LEFT
        else -> null
    }

    fun handleGesture(gesture: GestureType) {
        val context = if (drawerOpen) GestureContext.APP_DRAWER else GestureContext.HOME
        val action = handler.resolve(gesture, context)
        when {
            drawerOpen && (action == GestureAction.GO_HOME || (action == null && openingGesture?.let { reverseOf(it) == gesture } == true)) -> {
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
            !drawerOpen && context == GestureContext.HOME && gesture in listOf(GestureType.SWIPE_LEFT, GestureType.SWIPE_RIGHT) && (action == null || action == GestureAction.NONE) -> {
                homePage = (homePage + if (gesture == GestureType.SWIPE_LEFT) 1 else -1).coerceIn(0, homePageCount - 1)
            }
            drawerOpen && gesture in listOf(GestureType.SWIPE_LEFT, GestureType.SWIPE_RIGHT) && (action == null || action == GestureAction.NONE) -> {
                drawerPage = (drawerPage + if (gesture == GestureType.SWIPE_LEFT) 1 else -1).coerceIn(0, drawerPageCount - 1)
            }
            else -> Unit
        }
    }


    if (drawerOpen) {
        AppScreen(
            apps = apps,
            backgroundUri = backgroundUri,
            layout = layout,
            drawerPage = drawerPage,
            drawerPageCount = drawerPageCount,
            transitionStyle = transitionStyle,
            onDrawerPageSelected = { drawerPage = it.coerceIn(0, drawerPageCount - 1) },
            onAddHomeScreen = {
                homePageCount += 1
                homePage = homePageCount - 1
                onHomePageCountChange(homePageCount)
            },
            onPickBackground = onPickBackground,
            onLayoutChange = onLayoutChange,
            onTransitionStyleChange = onTransitionStyleChange,
            onAppClick = onAppClick,
            onGesture = ::handleGesture
        )
    } else {
        HomeScreen(
            backgroundUri = backgroundUri,
            page = homePage,
            pageCount = homePageCount,
            transitionStyle = transitionStyle,
            onPageSelected = { homePage = it.coerceIn(0, homePageCount - 1) },
            onGesture = ::handleGesture
        )
    }
}

