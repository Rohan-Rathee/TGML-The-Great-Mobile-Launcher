package com.example.tgml_the_great_mobile_launcher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.tgml_the_great_mobile_launcher.ui.theme.TGMLTheGreatMobileLauncherTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class AppInfo(
    val name: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apps = getInstalledApps()

        setContent {
            TGMLTheGreatMobileLauncherTheme {
                AppDrawer(
                    apps = apps,
                    onAppClick = { app ->
                        launchApp(app)
                    }
                )
            }
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

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
        val intent = Intent().apply {
            setClassName(
                app.packageName,
                app.activityName
            )
        }

        startActivity(intent)
    }
}

@Composable
fun AppDrawer( apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit) {
    var rotation by remember { mutableFloatStateOf(0f) }

    val sortedApps = remember(apps) {
        apps.sortedBy { it.name.lowercase() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    rotation += dragAmount.y * 0.25f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        RadialApps(
            apps = apps,
            rotation = rotation,
            onAppClick = onAppClick
        )
    }
}

@Composable
fun RadialApps( apps: List<AppInfo>, rotation: Float, onAppClick: (AppInfo) -> Unit)  {
    val radius = 1000f
    val visibleAngle = 70f
    val centerAngle = 0f

    val anglePerApp = 12f

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        apps.forEachIndexed { index, app ->
            val angle = index * anglePerApp + rotation

            val normalizedAngle = ((angle + 180f) % 360) - 180f

            if (kotlin.math.abs(normalizedAngle - centerAngle) <= visibleAngle / 2f) {
                val radians = Math.toRadians(angle.toDouble())

                val x = cos(radians) * radius
                val y = sin(radians) * radius

                AppIcon(
                    app = app,
                    x = x.toFloat()-700f,
                    y = y.toFloat()+1000f,
                    onClick = {
                        onAppClick(app)
                    }
                )
            }
        }
    }
}

@Composable
fun AppIcon( app: AppInfo, x: Float, y: Float, onClick: () -> Unit ) {
    val bitmap = remember(app.icon) {
        app.icon.toBitmap(
            width = 128,
            height = 128
        ).asImageBitmap()
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x.roundToInt(),
                    y.roundToInt()
                )
            }
            .size(72.dp)
            .clickable {
                onClick()
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = app.name,
            modifier = Modifier
                .size(56.dp)
        )
    }
}