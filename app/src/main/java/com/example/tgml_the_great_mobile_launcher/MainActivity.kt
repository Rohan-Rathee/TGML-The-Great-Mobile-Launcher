package com.example.tgml_the_great_mobile_launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.core.net.toUri
import com.example.tgml_the_great_mobile_launcher.data.GesturePreferences
import com.example.tgml_the_great_mobile_launcher.ui.LauncherNavigation
import com.example.tgml_the_great_mobile_launcher.ui.PageTransitionStyle
import com.example.tgml_the_great_mobile_launcher.ui.theme.TGMLTheGreatMobileLauncherTheme

class MainActivity : ComponentActivity()
{
	private val selectedBackgroundUri = mutableStateOf<Uri?>(null)
	private val selectedLayout = mutableStateOf(LauncherLayout.RADIAL)
    private val selectedTransitionStyle = mutableStateOf(PageTransitionStyle.SLIDE)
	private val backgroundPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		if (uri != null) {
			try{
				contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
			} catch (_: Exception) {

			}
			getSharedPreferences("launcher", MODE_PRIVATE)
				.edit {
    				putString("background_uri", uri.toString())
				}
			selectedBackgroundUri.value = uri
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		val apps = getInstalledApps()
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
		setContent { TGMLTheGreatMobileLauncherTheme { LauncherNavigation (
			apps = apps,
			backgroundUri = selectedBackgroundUri.value,
			layout = selectedLayout.value,
			gestureMappings = gestureMappings,
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
			onLayoutChange = { newLayout ->
				selectedLayout.value = newLayout

				getSharedPreferences("launcher", MODE_PRIVATE)
					.edit {
						putString("layout", newLayout.name)
					}
			},
			onAppClick = { app ->
				launchApp(app)
			}
		)}}
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
            .sortedBy { it.name.lowercase() }
    }

    private fun launchApp(app: AppInfo) {
        val intent = Intent().apply { setClassName(app.packageName, app.activityName) }
        startActivity(intent)
    }
}
