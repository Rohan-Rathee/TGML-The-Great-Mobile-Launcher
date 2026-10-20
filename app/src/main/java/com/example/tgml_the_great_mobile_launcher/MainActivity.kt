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
import com.example.tgml_the_great_mobile_launcher.ui.theme.TGMLTheGreatMobileLauncherTheme

class MainActivity : ComponentActivity()
{
	private val selectedBackgroundUri = mutableStateOf<Uri?>(null)
	private val selectedLayout = mutableStateOf(LauncherLayout.RADIAL)
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
		val gestureMappings = GesturePreferences.load(this)
		setContent { TGMLTheGreatMobileLauncherTheme { LauncherNavigation (
			apps = apps,
			backgroundUri = selectedBackgroundUri.value,
			layout = selectedLayout.value,
			gestureMappings = gestureMappings,
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
