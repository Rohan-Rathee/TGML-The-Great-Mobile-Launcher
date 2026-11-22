package com.example.tgml_the_great_mobile_launcher

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager.LayoutParams
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.tgml_the_great_mobile_launcher.commandcenter.AppUsageStore
import com.example.tgml_the_great_mobile_launcher.data.GesturePreferences
import com.example.tgml_the_great_mobile_launcher.privacy.AppLockGateActivity
import com.example.tgml_the_great_mobile_launcher.privacyobject.AppPrivacyPreferences
import com.example.tgml_the_great_mobile_launcher.ui.LauncherNavigation
import com.example.tgml_the_great_mobile_launcher.ui.PageTransitionStyle
import com.example.tgml_the_great_mobile_launcher.ui.theme.TGMLTheGreatMobileLauncherTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity()
{
	private val selectedLayout = mutableStateOf(LauncherLayout.RADIAL)
	private val selectedTransitionStyle = mutableStateOf(PageTransitionStyle.SLIDE)
	private val sideHandleEnabled = mutableStateOf(false)
	private val sideHandleHomeIntegration = mutableStateOf(false)
	private val sideHandleFolders = mutableStateOf<List<SideHandleFolder>>(emptyList())

	private val selectedBackgroundUri = mutableStateOf<Uri?>(null)
	private val appList = mutableStateOf<List<AppInfo>>(emptyList())

	private val backgroundPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		if (uri != null) {
			try {
				contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
			} catch (_: SecurityException) {}

			selectedBackgroundUri.value = uri
			getSharedPreferences("launcher", MODE_PRIVATE).edit {
				putString("background_uri", uri.toString())
			}
		}
	}


	override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		window.addFlags(LayoutParams.FLAG_SHOW_WALLPAPER)
		window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

		selectedBackgroundUri.value = getSharedPreferences("launcher", MODE_PRIVATE)
			.getString("background_uri", null)?.let { Uri.parse(it) }
		appList.value = getInstalledApps()
		lifecycleScope.launch {
			AppPrivacyPreferences.version.collect { appList.value = getInstalledApps() }
		}
		val savedBackground =
			getSharedPreferences("launcher", MODE_PRIVATE)
				.getString("background_uri", null)

		selectedBackgroundUri.value = savedBackground?.toUri()
		val savedLayout = getSharedPreferences("launcher", MODE_PRIVATE)
			.getString("layout",  LauncherLayout.RADIAL.name)

		selectedLayout.value = try {
			LauncherLayout.valueOf(savedLayout ?: LauncherLayout.RADIAL.name)
		} catch (_: Exception) {
			LauncherLayout.RADIAL
		}
		val savedTransition = getSharedPreferences("launcher", MODE_PRIVATE).getString("page_transition", PageTransitionStyle.SLIDE.name)
		selectedTransitionStyle.value = try {
			PageTransitionStyle.valueOf(savedTransition ?: PageTransitionStyle.SLIDE.name)
		} catch (_: Exception) {
			PageTransitionStyle.SLIDE
		}
		val homePageCount = getSharedPreferences("launcher", MODE_PRIVATE).getInt("home_page_count", 1).coerceAtLeast(1)
		val gestureMappings = GesturePreferences.load(this)
		sideHandleEnabled.value = SideHandlePreferences.isEnabled(this)
		sideHandleHomeIntegration.value = SideHandlePreferences.homeIntegration(this)
		sideHandleFolders.value = SideHandlePreferences.loadFolders(this)

		setContent { TGMLTheGreatMobileLauncherTheme { LauncherNavigation (
			apps = appList.value,
			backgroundUri = selectedBackgroundUri.value,
			layout = selectedLayout.value,
			gestureMappings = gestureMappings,
			sideHandleEnabled = sideHandleEnabled.value,
			sideHandleHomeIntegration = sideHandleHomeIntegration.value,
			sideHandleFolders = sideHandleFolders.value,
			onSideHandleEnabledChange = { enabled ->
				sideHandleEnabled.value = enabled
				SideHandlePreferences.setEnabled(this, enabled)

				if (enabled) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
						val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
						startActivity(intent)
					} else {
						SideHandleService.start(this)
					}
				} else {
					SideHandleService.stop(this)
				}
			},
			onSideHandleHomeIntegrationChange = { enabled ->
				sideHandleHomeIntegration.value = enabled
				SideHandlePreferences.setHomeIntegration(this, enabled)
			},
			onSideHandleFoldersChange = { folders ->
				sideHandleFolders.value = folders
				SideHandlePreferences.saveFolders(this, folders)
				if (sideHandleEnabled.value) {
					startService(Intent(this, SideHandleService::class.java).setAction(SideHandleService.ACTION_REFRESH))
				}
			},
			initialHomePageCount = homePageCount,
			transitionStyle = selectedTransitionStyle.value,
			onHomePageCountChange = { count ->
				getSharedPreferences("launcher", MODE_PRIVATE).edit {
					putInt("home_page_count", count.coerceAtLeast(1))
				}
			},
			onTransitionStyleChange = { style ->
				selectedTransitionStyle.value = style
				getSharedPreferences("launcher", MODE_PRIVATE).edit {
					putString("page_transition", style.name)
				}
			},
			onPickBackground = {
				backgroundPicker.launch(arrayOf("image/*"))
			},
			onUseSystemWallpaper = {
				selectedBackgroundUri.value = null
				getSharedPreferences("launcher", MODE_PRIVATE).edit {
					remove("background_uri")
				}
			},
			onOpenSystemWallpaper = {
				startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
			},

			onLayoutChange = { newLayout ->
				selectedLayout.value = newLayout

				getSharedPreferences("launcher", MODE_PRIVATE)
					.edit {
						putString("layout", newLayout.name)
					}
			},
			onAppClick = { app ->
				AppUsageStore.recordLaunch(this, app)
				launchApp(app)
			}
		)}}
	}

	override fun onResume() {
		super.onResume()

		if (sideHandleEnabled.value) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
				SideHandleService.start(this)
			} else {
				SideHandlePreferences.setEnabled(this, false)
				sideHandleEnabled.value = false
				SideHandleService.stop(this)
			}
		}
	}

	override fun onStop() {
		if (sideHandleEnabled.value) {
			startService(
				Intent(this, SideHandleService::class.java)
					.setAction(SideHandleService.ACTION_HOME_MODE)
					.putExtra(SideHandleService.EXTRA_HOME_MODE, false)
			)
		}
		super.onStop()
	}

	private fun getInstalledApps(): List<AppInfo> {
		val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }

		return packageManager
			.queryIntentActivities(intent, 0)
			.map {
				AppInfo(
					name = it.loadLabel(packageManager).toString(),
					packageName = it.activityInfo.packageName,
					activityName = it.activityInfo.name,
					icon = it.loadIcon(packageManager)
				)
			}
			.filterNot { AppPrivacyPreferences.isHidden(this, it.packageName) }
			.sortedBy { it.name.lowercase() }
	}

	private fun launchApp(app: AppInfo) {
		if (AppPrivacyPreferences.isLocked(this, app.packageName)) {
			startActivity(AppLockGateActivity.intent(this, app.packageName, app.activityName))
			return
		}
		val intent = Intent().apply { setClassName(app.packageName, app.activityName) }
		startActivity(intent)
	}
}
