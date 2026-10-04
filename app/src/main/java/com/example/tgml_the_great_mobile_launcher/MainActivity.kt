package com.example.tgml_the_great_mobile_launcher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.tgml_the_great_mobile_launcher.ui.theme.TGMLTheGreatMobileLauncherTheme
import kotlin.math.cos

import kotlin.math.roundToInt
import kotlin.math.sin

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment


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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        RadialApps(
            apps = apps,
            rotation = rotation,
            onRotationChange = { rotation = it },
            onAppClick = onAppClick
        )
    }
}

@Composable
fun RadialApps(
    apps: List<AppInfo>,
    rotation: Float,
    onRotationChange: (Float) -> Unit,
    onAppClick: (AppInfo) -> Unit){

    if (apps.isEmpty()) return

    BoxWithConstraints( modifier = Modifier.fillMaxSize() ) {
        val density = LocalDensity.current

        val screenWidth = with(density) { maxWidth.toPx() }
        val screenHeight = with(density) { maxHeight.toPx() }

        val visibleApps = 7
        val visibleAngle = 62f

        val angleStep = 360f/apps.size
        val iconSpacing = 300f
        val angleRadians = Math.toRadians(angleStep.toDouble())
        val radius = iconSpacing / (2f * sin(angleRadians / 2f).toFloat())

        val centerAngle = 0f

        var currentRotation by remember { mutableFloatStateOf(rotation) }

        LaunchedEffect(rotation) {
            currentRotation = rotation
        }


        val centerX = (screenWidth * 0.50f)- radius
        val centerY = screenHeight * 0.50f

        val maxRotation = 0f
        val minRotation = -(apps.size - visibleApps).coerceAtLeast(0) * angleStep

        val clampedRotation = currentRotation.coerceIn(
            minRotation,
            maxRotation
        )

        if (clampedRotation != currentRotation) {
            currentRotation = clampedRotation
            onRotationChange(clampedRotation)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(apps.size, radius) {
                    detectDragGestures() { _, dragAmount ->
                        val sensitivity = 180f / (radius * Math.toRadians(180f.toDouble())).toFloat()
                        val delta = dragAmount.y * sensitivity

                        val newRotation =
                            (currentRotation + delta).coerceIn(minRotation, maxRotation)

                        currentRotation = newRotation
                        onRotationChange(newRotation)
                    }
                }
        ) {
            apps.forEachIndexed { index, app ->
                val angle = centerAngle + index * angleStep + currentRotation
                val radians = Math.toRadians(angle.toDouble())

                val x = centerX + cos(radians) * radius
                val y = centerY + sin(radians) * radius

                if (
                    x > -150f &&
                    x < screenWidth + 150f &&
                    y > -150f &&
                    y < screenHeight + 150f
                ) {
                    AppIcon(
                        app = app,
                        x = x.toFloat(),
                        y = y.toFloat(),
                        onClick = {
                            onAppClick(app)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppIcon(
    app: AppInfo,
    x: Float,
    y: Float,
    onClick: () -> Unit
) {
    val iconSize = 64.dp
    val itemWidth = 180.dp
    val itemHeight = 70.dp

    val density = LocalDensity.current
    val itemWidthPx = with(density) { itemWidth.toPx() }
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val bitmap = remember(app.icon) {
        app.icon.toBitmap(
            width = 128,
            height = 128
        ).asImageBitmap()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .size(
                width = itemWidth,
                height = itemHeight
            )
            .offset {
                IntOffset(
                    x.roundToInt() - (itemWidthPx / 2).roundToInt(),
                    y.roundToInt() - (itemHeightPx / 2).roundToInt()
                )
            }
            .clickable {
                onClick()
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = app.name,
            modifier = Modifier.size(iconSize)
        )

        Text(
            text = app.name,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}