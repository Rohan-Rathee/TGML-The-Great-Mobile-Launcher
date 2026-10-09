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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable as Composable
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.tgml_the_great_mobile_launcher.ui.theme.TGMLTheGreatMobileLauncherTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.produceState 
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.compose.ui.draw.blur


data class AppInfo(
    val name: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable
)

enum class LauncherLayout
{
	RADIAL,
	GRID_SCROLL,
	GRID_ZOOM
}

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


	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
		setContent { TGMLTheGreatMobileLauncherTheme { HomeScreen(
			apps = apps,
			backgroundUri = selectedBackgroundUri.value,
			layout = selectedLayout.value,
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

@Composable
fun HomeScreen(apps: List<AppInfo>, backgroundUri: Uri?, layout: LauncherLayout, onPickBackground: () -> Unit, onLayoutChange: (LauncherLayout) -> Unit, onAppClick: (AppInfo) -> Unit)
{
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
				detectTapGestures(
					onLongPress = {
						editMode = true
					}
				)
			}
	) {
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
		) {

			LauncherBackground(
				backgroundUri = backgroundUri
			)

			LauncherLayoutView(
				layout = layout,
				apps = apps,
				onAppClick = onAppClick
			)
		}
		AnimatedVisibility(
			visible = editMode,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.navigationBarsPadding(),
			enter = fadeIn(tween (200)) + slideInVertically(initialOffsetY = {it}, animationSpec = tween (300)),
			exit = fadeOut(tween (200)) + slideOutVertically(targetOffsetY = {it}, animationSpec = tween (300)),

			) {
			EditorBar(
				onBackgroundClick = onPickBackground,
				onAddScreen = {
				},
				onSettingsClick = {
					settingsOpen = true
				},
				onDone = {
					editMode = false
				}
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
				onClose = {
					settingsOpen = false
				}
			)
		}
	}
}

@Composable
fun LauncherSettings(currentLayout: LauncherLayout, onLayoutChange: (LauncherLayout) -> Unit, onClose: () -> Unit)
{
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.65f))
	) {
		Column(
			modifier = Modifier
				.align(Alignment.Center)
				.background(
					Color.Black.copy(alpha = 0.9f),
					RoundedCornerShape(24.dp)
				)
				.padding(24.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "Launcher Layout",
				color = Color.White,
				fontSize = 22.sp
			)

			LayoutOption(
				name = "Radial",
				selected = currentLayout == LauncherLayout.RADIAL,
				enabled = true,
				onClick = {
					onLayoutChange(LauncherLayout.RADIAL)
				}
			)

			LayoutOption(
				name = "Grid Scroll",
				selected = currentLayout == LauncherLayout.GRID_SCROLL,
				enabled = true,
				onClick = {
					onLayoutChange(LauncherLayout.GRID_SCROLL)
				}
			)

			LayoutOption(
				name = "Grid Zoom",
				selected = currentLayout == LauncherLayout.GRID_ZOOM,
				enabled = true,
				onClick = {
					onLayoutChange(LauncherLayout.GRID_ZOOM)
				}
			)

			Text(
				text = "Close",
				color = Color.White,
				modifier = Modifier
					.clickable {
						onClose()
					}
					.padding(8.dp)
			)
		}
	}
}

@Composable
fun LayoutOption(name: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit)
{
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.clickable(enabled = enabled) {
				onClick()
			}
			.padding(vertical = 10.dp)
	) {
		Text(
			text = if (selected) "●" else "○",
			color = if (enabled) Color.White else Color.Gray,
			fontSize = 18.sp
		)

		Text(
			text = name,
			color = if (enabled) {
				Color.White
			} else {
				Color.White.copy(alpha = 0.35f)
			},
			fontSize = 16.sp,
			modifier = Modifier.padding(start = 12.dp)
		)

		if (!enabled) {
			Text(
				text = "Coming soon",
				color = Color.White.copy(alpha = 0.3f),
				fontSize = 11.sp,
				modifier = Modifier.padding(start = 8.dp)
			)
		}
	}
}

@Composable
fun LauncherLayoutView(layout: LauncherLayout, apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit)
{
	when (layout) {
		LauncherLayout.RADIAL -> {
			RadialApps(
				apps = apps,
				onAppClick = onAppClick
			)
		}

		LauncherLayout.GRID_SCROLL -> {
			GridScroll(
				apps = apps,
				onAppClick = onAppClick
			)
		}

		LauncherLayout.GRID_ZOOM -> {
			GridZoom(
				apps = apps,
				onAppClick = onAppClick
			)
		}
	}
}

@Composable
fun LauncherBackground(backgroundUri: Uri?, blurred: Boolean = false)
{
	val context = LocalContext.current
	val bitmap by produceState<android.graphics.Bitmap?> (
		initialValue = null,
		key1 = backgroundUri
	){
		value = if (backgroundUri == null){
			null
		} else{
			withContext(Dispatchers.IO){
				try {
					context.contentResolver.openInputStream(backgroundUri)?.use { BitmapFactory.decodeStream(it) }
				} catch (_: Exception){
					null
				}
			}
		}
	}

	if (bitmap != null) {

		Image(
			bitmap = bitmap!!.asImageBitmap(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)

	} else {

		Image(
			painter = painterResource(
				id = R.drawable.launcher_background
			),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxSize()
				.then(
					if (blurred) {
						Modifier.blur(35.dp)
					} else {
						Modifier
					}
				)
		)
	}
}

@Composable
fun EditorBar(onBackgroundClick: () -> Unit, onAddScreen: () -> Unit, onSettingsClick: () -> Unit, onDone: () -> Unit)
{
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.padding(horizontal = 12.dp, vertical = 12.dp)
			.background(
				Color.Black.copy(alpha = 0.82f),
				RoundedCornerShape(22.dp)
			)
			.padding(horizontal = 10.dp, vertical = 8.dp)
	) {
		EditorButton(
			icon = "🎨",
			label = "Background",
			onClick = onBackgroundClick
		)
		EditorButton(
				icon = "+",
		label = "Add screen",
		onClick = onAddScreen
		)

		EditorButton(
			icon = "⚙",
			label = "Settings",
			onClick = onSettingsClick
		)

		EditorButton(
			icon = "✓",
			label = "Done",
			onClick = onDone
		)
	}
}

@Composable
fun RadialApps(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit)
{
    if (apps.isEmpty()) return

    var rotation by remember { mutableFloatStateOf(-45f) }
    var momentumJob by remember { mutableStateOf<Job?>(null) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val velocityTracker = remember { VelocityTracker() }
        val screenWidth = with(density) { maxWidth.toPx() }
        val screenHeight = with(density) { maxHeight.toPx() }
	    val appsPerLayer = 15
	    val layerCount = (apps.size+appsPerLayer-1)/appsPerLayer

        val angleStep = 180f/appsPerLayer
        val iconSpacing = 220f
        val angleRadians = Math.toRadians(angleStep.toDouble())
        val radius = iconSpacing / (2f * sin(angleRadians / 2f).toFloat())
        val centerX = screenWidth * 0.75f - radius
        val centerY = screenHeight * 0.50f

	    val minRotation = -(layerCount) * 180f + 90
	    val maxRotation = 0f
	    val alphabet = ('A'..'Z').toList()
	    val alphabetIndices = remember(apps) {
		    alphabet.associateWith { letter ->
			    apps.indexOfFirst {
				    it.name.firstOrNull()?.uppercaseChar() == letter
			    }
		    }
	    }
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

			            onDrag = { change, dragAmount ->
				            change.consume()
				            velocityTracker.addPosition(change.uptimeMillis, change.position)
				            val sensitivity = 180f / (radius * Math.toRadians(180.0).toFloat())

				            dragRotation = (dragRotation + dragAmount.y * sensitivity).coerceIn(
					            minRotation,
					            maxRotation
				            )
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

						            dragRotation =
							            (dragRotation + angularVelocity * deltaTime).coerceIn(
								            minRotation,
								            maxRotation
							            )

						            rotation = dragRotation

						            if (dragRotation == minRotation || dragRotation == maxRotation) {
							            break
						            }

						            angularVelocity *= exp(-0.5f * deltaTime)
					            }
					            momentumJob = null
				            }
			            }
		            )
	            }
        ) {
			//remember this
		    val layerPosition = -rotation / 180f

		    val currentLayer = (layerPosition + 0.5f)
			    .toInt()
			    .coerceIn(0, layerCount - 1)
		    val visibleLayers = listOf(
			    currentLayer,
			    currentLayer + 1
		    )


            apps.forEachIndexed { index, app ->
				val layer = index/appsPerLayer + 1
	            if (layer in visibleLayers){

					val localIndex = index % appsPerLayer
	                val angle = localIndex * angleStep + rotation + (layer-1) * 180f
	                val radians = Math.toRadians(angle.toDouble())

	                val x = centerX + cos(radians).toFloat() * radius
	                val y = centerY + sin(radians).toFloat() * radius

	                if (x > -200f && x < screenWidth + 200f && y > -200f && y < screenHeight + 200f) {
	                    val angleDistance = abs(angle) % 360f
	                    val shortestAngleDistance = minOf(angleDistance, 360f - angleDistance)
	                    val appDistance = shortestAngleDistance / angleStep
	                    val scale = 0.5f + (1.2f - 0.5f) * exp(-0.35f * appDistance)
		                val fadeDistance = (appDistance / 7f).coerceIn(0f, 1f)
		                val alpha = 1f - fadeDistance * fadeDistance * (3f - 2f * fadeDistance)
		                AppIcon(app = app, x = x, y = y, scale = scale, alpha = alpha, onClick = { onAppClick(app) })
	                }
				}
            }
        }

	    //alphabet bar
	    Column(
		    modifier = Modifier
			    .align(Alignment.CenterEnd)
			    .padding(end = 8.dp)
			    .height(700.dp)
			    .pointerInput(Unit) {
				    detectTapGestures { position ->
					    val fraction = (position.y / size.height).coerceIn(0f, 1f)

					    val letterIndex = (fraction * (alphabet.size - 1)).roundToInt().coerceIn(0, alphabet.size - 1)

					    val letter = alphabet[letterIndex]
					    val index = alphabetIndices[letter] ?: -1

					    if (index >= 0) { rotation = (-index * angleStep).coerceIn(minRotation, maxRotation)
						    momentumJob?.cancel()
					    }
				    }
			    }
			    .pointerInput(Unit) {
				    detectVerticalDragGestures { change, _ ->
					    change.consume()

					    val fraction = (change.position.y / size.height).coerceIn(0f, 1f)

					    val letterIndex = (fraction * (alphabet.size - 1)).roundToInt().coerceIn(0, alphabet.size - 1)

					    val letter = alphabet[letterIndex]
					    val index = alphabetIndices[letter] ?: -1

					    if (index >= 0) { rotation = (-index * angleStep).coerceIn(minRotation, maxRotation)
						    momentumJob?.cancel()
					    }
				    }
			    },
			verticalArrangement = Arrangement.SpaceEvenly
		) {
			alphabet.forEach { letter->
				Text(
					text = letter.toString(),
					color = Color.White,
					fontSize = 12.sp,

				)
			}
	    }
    }
}


@Composable
fun GridScroll(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit)
{
	LazyVerticalGrid(
		columns = GridCells.Fixed(5),
		modifier = Modifier
			.fillMaxSize()
			.padding(
				start = 40.dp,
				end = 40.dp,
				top = 40.dp,
				bottom = 40.dp
			),
		verticalArrangement = Arrangement.spacedBy(20.dp),
		horizontalArrangement = Arrangement.spacedBy(20.dp)
	) {
		items(apps) { app ->
			GridAppIcon(
				app = app,
				onClick = { onAppClick(app) }
			)
		}
	}
}

@Composable
fun GridZoom(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit)
{
	if (apps.isEmpty()) return
	var hoveredIndex by remember { mutableStateOf<Int?>(null)}
	BoxWithConstraints(
		modifier = Modifier.fillMaxSize()
	){
		val density = LocalDensity.current
		val screenWidth = with(density){ maxWidth.toPx() }
		val screenHeight = with(density){ maxHeight.toPx() }

		val cellWidth = with(density) { 90.dp.toPx() }
		val cellHeight = with(density) { 75.dp.toPx() }

		val bestLayout = (1..apps.size).map { cols ->
			val rows = (apps.size + cols - 1) / cols
			val scaleX = screenWidth / (cols * cellWidth)
			val scaleY = screenHeight / (rows * cellHeight)
			Triple(cols, rows, minOf(scaleX, scaleY, 1f))
		}.maxByOrNull { it.third } !!
		ZoomGridContent(
			apps =  apps,
			columns = bestLayout.first,
			scale = bestLayout.third,
			cellWidthPx = cellWidth,
			cellHeightPx = cellHeight,
			hoveredIndex = hoveredIndex,
			onHover = { index -> hoveredIndex = index },
			onRelease = {
				hoveredIndex?.let{ index ->
					if (index in apps.indices) {
						onAppClick(apps[index])
					}
				}
			}
		)
	}
}

@Composable
fun ZoomGridContent(apps: List<AppInfo>, columns: Int, scale: Float, cellWidthPx: Float, cellHeightPx: Float, hoveredIndex: Int?, onHover: (Int?) -> Unit, onRelease: () -> Unit)
{
	BoxWithConstraints(
		modifier = Modifier
			.fillMaxSize()
			.pointerInput(apps,scale) {
				detectDragGestures (
					onDragStart = { position ->
						onHover(
							findGridItem(
								position.x,
								position.y,
								apps.size,
								columns,
								scale,
								size.width,
								size.height,
								cellWidthPx, cellHeightPx
							)
						)
					},
					onDrag = { change, _ ->
						change.consume()
						onHover(
							findGridItem(
								change.position.x,
								change.position.y,
								apps.size,
								columns,
								scale,
								size.width,
								size.height,
								cellWidthPx, cellHeightPx
							)
						)
					},
					onDragEnd = {
						onRelease()
						onHover(null)
					},
					onDragCancel = {
						onHover(null)
					}
				)
			}
	) {

		val screenWidth = with(LocalDensity.current) { maxWidth.toPx() }
		val screenHeight = with(LocalDensity.current) { maxHeight.toPx() }
		val rows = (apps.size + columns - 1) / columns
		val gridWidth = columns * cellWidthPx * scale
		val gridHeight = rows * cellHeightPx * scale
		val startX = (screenWidth - gridWidth) / 2f
		val startY = (screenHeight - gridHeight) / 2f

		apps.forEachIndexed { index, app ->
			val row = index/columns
			val column = index % columns

			val x = startX + (column + 0.5f) * cellWidthPx * scale
			val y = startY + (row + 0.5f) * cellHeightPx * scale

			val distance = if (hoveredIndex != null) {
				gridDistance(index, hoveredIndex, columns)
			} else {
				100f
			}

			val itemScale = if (hoveredIndex == null){
				1f
			} else {
				1f + 0.8f * kotlin.math.exp(-0.45f * distance)
			}

			GridZoomAppIcon(
				app = app,
				x = x,
				y = y,
				scale = scale *itemScale,
			)
		}
	}
}

fun findGridItem( x:Float, y:Float, appCount: Int, columns: Int, scale: Float, screenWidth: Int, screenHeight: Int, cellWidthPx: Float, cellHeightPx: Float): Int?{
	if (columns <= 0 || scale <= 0f) return null

	val cellWidth = cellWidthPx * scale
	val cellHeight = cellHeightPx * scale

	val rows = (appCount + columns - 1) / columns
	val gridWidth = columns * cellWidth
	val gridHeight = rows * cellHeight

	val startX = (screenWidth - gridWidth) / 2f
	val startY = (screenHeight - gridHeight) / 2f
	if (x < startX || x >= startX + gridWidth ||
		y < startY || y >= startY + gridHeight
	) return null
	val column = ((x - startX) / cellWidth).toInt()
	val row = ((y - startY) / cellHeight).toInt()

	val index = row * columns + column
	return if (index in 0 until appCount){
		index
	} else {
		null
	}
}

fun gridDistance( first: Int, second: Int, columns: Int): Float{
	val firstRow = first/columns
	val firstColumn = first % columns
	val secondRow = second/columns
	val secondColumn = second % columns
	val dx = (firstColumn - secondColumn).toFloat()
	val dy = (firstRow - secondRow).toFloat()

	return kotlin.math.sqrt(dx*dx + dy*dy)
}

@Composable
fun GridAppIcon(app: AppInfo, onClick: () -> Unit)
{
	val bitmap = remember(app.icon) {
		app.icon
			.toBitmap(width = 128, height = 128)
			.asImageBitmap()
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.clickable {
				onClick()
			}
			.padding(8.dp)
	) {
		Image(
			bitmap = bitmap,
			contentDescription = app.name,
			modifier = Modifier.size(64.dp)
		)

		Text(
			text = if (app.name.length > 10) {
				app.name.take(7) + "..."
			} else {
				app.name
			},
			color = Color.White,
			maxLines = 1,
			fontSize = 12.sp
		)
	}
}

@Composable
fun AppIcon(app: AppInfo, x: Float, y: Float, scale: Float, alpha: Float, onClick: () -> Unit)
{
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
	            this.alpha = alpha
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
		    text = if (app.name.length > 10) {
			    app.name.take(7) + "..."
		    } else {
			    app.name
		    },
            color = Color.White,
            maxLines = 1,
	        fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun GridZoomAppIcon(
	app: AppInfo,
	x: Float,
	y: Float,
	scale: Float
)
{
	val iconSize = 40.dp
	val itemWidth = 90.dp
	val itemHeight = 75.dp
	val density = LocalDensity.current
	val itemWidthPx = with(density) { itemWidth.toPx() }
	val itemHeightPx = with(density) { itemHeight.toPx() }

	val bitmap = remember(app.icon) { app.icon.toBitmap(width = 128, height = 128).asImageBitmap() }
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.size(width = itemWidth, height = itemHeight)
			.offset{
				IntOffset(
					x.roundToInt() - (itemWidthPx/2).roundToInt(),
					y.roundToInt() - (itemHeightPx/2).roundToInt()
				)
			}
			.graphicsLayer(scaleX = scale, scaleY = scale)
	) {
		Image(
			bitmap = bitmap,
			contentDescription = app.name,
			modifier = Modifier.size(iconSize)
		)
		Text(
			text = if (app.name.length > 10) {
				app.name.take(7) + "..."
			} else {
				app.name
			},
			color = Color.White,
			maxLines = 1,
			fontSize = 10.sp

		)

	}
}

@Composable
fun EditorButton(icon: String, label: String, onClick: () -> Unit )
{
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.pointerInput(Unit) {
				detectTapGestures {
					onClick()
				}
			}
			.padding(
				horizontal = 8.dp,
				vertical = 4.dp
			)
	) {

		Text(
			text = icon,
			color = Color.White,
			fontSize = 22.sp
		)
		Text(
			text = label,
			color = Color.White.copy(alpha = 0.75f),
			fontSize = 9.sp
		)
	}
}