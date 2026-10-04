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
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.exp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

data class AppInfo(
    val name: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apps = getInstalledApps()

        setContent { TGMLTheGreatMobileLauncherTheme { HomeScreen(apps = apps, onAppClick = { app -> launchApp(app) })}}
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

@Composable
fun HomeScreen(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) { RadialApps( apps = apps, onAppClick = onAppClick) }
}

@Composable
fun RadialApps(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit) {
    if (apps.isEmpty()) return

    var rotation by remember { mutableFloatStateOf(0f) }
    var momentumJob by remember { mutableStateOf<Job?>(null) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val velocityTracker = remember { VelocityTracker() }

        val screenWidth = with(density) { maxWidth.toPx() }
        val screenHeight = with(density) { maxHeight.toPx() }

        val angleStep = 360f / 150f
        val iconSpacing = 250f

        val angleRadians = Math.toRadians(angleStep.toDouble())

        val radius = iconSpacing / (2f * sin(angleRadians / 2f).toFloat())

        val centerX = screenWidth * 0.50f - radius

        val centerY = screenHeight * 0.50f

        val minRotation = -(apps.size - 1) * angleStep

        val maxRotation = 0f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var dragRotation = 0f

                    detectDragGestures(
                        onDragStart = {
                            momentumJob?.cancel()
                            dragRotation = rotation
                            velocityTracker.resetTracking()
                        },

                        onDrag = { change, dragAmount -> change.consume()
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            val sensitivity = 180f / (radius * Math.toRadians(180.0).toFloat())

                            dragRotation = (dragRotation + dragAmount.y * sensitivity).coerceIn(minRotation, maxRotation)
                            rotation = dragRotation
                        },

                        onDragEnd = {
                            val velocity = velocityTracker.calculateVelocity()
                            val sensitivity = 180f / (radius * Math.toRadians(180.0).toFloat())
                            var angularVelocity = velocity.y * sensitivity

                            momentumJob?.cancel()
                            momentumJob = scope.launch {
                                var lastTime = withFrameNanos { it }

                                while (
                                    isActive &&
                                    abs(angularVelocity) > 0.05f
                                ) {
                                    val currentTime = withFrameNanos { it }

                                    val deltaTime = (currentTime - lastTime) / 1_000_000_000f

                                    lastTime = currentTime

                                    dragRotation = (dragRotation + angularVelocity * deltaTime).coerceIn(minRotation, maxRotation)

                                    rotation = dragRotation

                                    if (dragRotation == minRotation || dragRotation == maxRotation) { break }

                                    angularVelocity *= exp(-0.5f * deltaTime)
                                }
                                momentumJob = null
                            }
                        }
                    )
                }
        ) {
            apps.forEachIndexed { index, app ->
                val angle = index * angleStep + rotation
                val radians = Math.toRadians(angle.toDouble())

                val x = centerX + cos(radians).toFloat() * radius
                val y = centerY + sin(radians).toFloat() * radius

                if (x > -200f && x < screenWidth + 200f && y > -200f && y < screenHeight + 200f) {
                    val angleDistance = abs(angle) % 360f
                    val shortestAngleDistance = minOf(angleDistance, 360f - angleDistance)
                    val appDistance = shortestAngleDistance / angleStep
                    val scale = 0.5f + (1.5f - 0.5f) * exp(-0.35f * appDistance)

                    AppIcon(app = app, x = x, y = y, scale = scale, onClick = { onAppClick(app) })
                }
            }
        }
    }
}

@Composable
fun AppIcon(app: AppInfo, x: Float, y: Float, scale: Float, onClick: () -> Unit) {
    val iconSize = 64.dp
    val itemWidth = 180.dp
    val itemHeight = 70.dp

    val density = LocalDensity.current

    val itemWidthPx = with(density) { itemWidth.toPx() }

    val itemHeightPx = with(density) { itemHeight.toPx() }

    val bitmap = remember(app.icon) { app.icon.toBitmap(width = 128, height = 128).asImageBitmap() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .size(width = itemWidth, height = itemHeight)
            .offset { IntOffset(x.roundToInt() - (itemWidthPx / 2).roundToInt(), y.roundToInt() - (itemHeightPx / 2).roundToInt()) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
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