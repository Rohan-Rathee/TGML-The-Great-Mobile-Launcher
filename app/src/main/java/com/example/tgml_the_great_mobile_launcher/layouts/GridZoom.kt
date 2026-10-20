package com.example.tgml_the_great_mobile_launcher.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.toSafeImageBitmap
import kotlin.math.roundToInt

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

		if (cellWidth <= 0f || cellHeight <= 0f) return@BoxWithConstraints

		val bestLayout = (1..apps.size).map { cols ->
			val rows = (apps.size + cols - 1) / cols
			val scaleX = if (cols * cellWidth > 0f) screenWidth / (cols * cellWidth) else 1f
			val scaleY = if (rows * cellHeight > 0f) screenHeight / (rows * cellHeight) else 1f
			Triple(cols, rows, minOf(scaleX, scaleY, 1f))
		}.maxByOrNull { it.third } ?: Triple(1, apps.size, 1f)
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

@Composable
fun GridZoomAppIcon(app: AppInfo, x: Float, y: Float, scale: Float)
{
	val iconSize = 40.dp
	val itemWidth = 90.dp
	val itemHeight = 75.dp
	val density = LocalDensity.current
	val itemWidthPx = with(density) { itemWidth.toPx() }
	val itemHeightPx = with(density) { itemHeight.toPx() }

	val bitmap = remember(key1 = app.packageName, key2 = app.activityName) {
		app.icon.toSafeImageBitmap(width = 128, height = 128)
	}
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
