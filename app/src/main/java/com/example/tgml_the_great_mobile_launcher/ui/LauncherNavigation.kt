package com.example.tgml_the_great_mobile_launcher.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.LauncherLayout
import com.example.tgml_the_great_mobile_launcher.gestures.GestureAction
import com.example.tgml_the_great_mobile_launcher.gestures.GestureContext
import com.example.tgml_the_great_mobile_launcher.gestures.GestureHandler
import com.example.tgml_the_great_mobile_launcher.gestures.GestureMapping
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import com.example.tgml_the_great_mobile_launcher.home.HomeController
import com.example.tgml_the_great_mobile_launcher.home.widgets.WidgetHostController

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
    val drawerPageCount = if (layout == LauncherLayout.MULTI_PAGE) {
		maxOf(1, (apps.size + APP_DRAWER_PAGE_SIZE - 1) / APP_DRAWER_PAGE_SIZE)
    } else 1
	val context = LocalContext.current
	val homeController = remember(context) {
		HomeController(context.applicationContext)
	}

	val widgetHost = remember(context) {
		WidgetHostController(context.applicationContext)
	}

	val widgetProviders = remember(widgetHost) {
		widgetHost.getInstalledProviders()
	}

	val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

	DisposableEffect(lifecycleOwner, widgetHost) {
		val observer = LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_START -> widgetHost.startListening()
				Lifecycle.Event.ON_STOP -> widgetHost.stopListening()
				else -> Unit
			}
		}

		lifecycleOwner.lifecycle.addObserver(observer)

		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	var pendingWidgetId by remember { mutableStateOf<Int?>(null) }
	var pendingWidgetProvider by remember {
		mutableStateOf<AppWidgetProviderInfo?>(null)
	}

	val configurationLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		val widgetId = pendingWidgetId

		if (widgetId != null) {
			if (
				result.resultCode == Activity.RESULT_OK &&
				homeController.addWidget(widgetId, homePage)
			) {
			} else {
				widgetHost.deleteWidget(widgetId)
			}
		}

		pendingWidgetId = null
		pendingWidgetProvider = null
	}

	val bindingLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		val widgetId = pendingWidgetId
		val provider = pendingWidgetProvider

		if (widgetId != null && provider != null) {
			if (result.resultCode == Activity.RESULT_OK) {
				if (provider.configure != null) {
					val configureIntent = Intent(
						AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
					).apply {
						component = provider.configure
						putExtra(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							widgetId
						)
					}

					configurationLauncher.launch(configureIntent)
				} else {
					if (!homeController.addWidget(widgetId, homePage)) {
						widgetHost.deleteWidget(widgetId)
					}

					pendingWidgetId = null
					pendingWidgetProvider = null
				}
			} else {
				widgetHost.deleteWidget(widgetId)
				pendingWidgetId = null
				pendingWidgetProvider = null
			}
		}
	}

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
	fun startWidgetBinding(provider: AppWidgetProviderInfo) {
		val widgetId = widgetHost.allocateWidgetId()

		pendingWidgetId = widgetId
		pendingWidgetProvider = provider

		val bound = widgetHost.bindWidget(
			widgetId = widgetId,
			provider = provider.provider
		)

		if (bound) {
			if (provider.configure != null) {
				val configureIntent = Intent(
					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
				).apply {
					component = provider.configure
					putExtra(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						widgetId
					)
				}

				configurationLauncher.launch(configureIntent)
			} else {
				if (!homeController.addWidget(widgetId, homePage)) {
					widgetHost.deleteWidget(widgetId)
				}

				pendingWidgetId = null
				pendingWidgetProvider = null
			}
		} else {
			val bindIntent = Intent(
				AppWidgetManager.ACTION_APPWIDGET_BIND
			).apply {
				putExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					widgetId
				)
				putExtra(
					AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
					provider.provider
				)
			}

			bindingLauncher.launch(bindIntent)
		}
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
	        onGesture = ::handleGesture,
	        layout = layout,
	        onAddHomeScreen = {
				homePageCount +=1
		        homePage = homePageCount -1
		        onHomePageCountChange(homePageCount)
	        },
	        onPickBackground = onPickBackground,
	        onLayoutChange = onLayoutChange,
	        onTransitionStyleChange = onTransitionStyleChange,
	        apps = apps,
	        homeController = homeController,
	        onAppClick = onAppClick,
	        widgetProviders = widgetProviders,
	        onWidgetSelected = ::startWidgetBinding,
	        widgetHost = widgetHost,
		)
    }
}