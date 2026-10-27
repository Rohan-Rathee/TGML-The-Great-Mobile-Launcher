
package com.example.tgml_the_great_mobile_launcher.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.AppInfo
import com.example.tgml_the_great_mobile_launcher.LauncherLayout
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import com.example.tgml_the_great_mobile_launcher.gestures.detectLauncherSwipes
import com.example.tgml_the_great_mobile_launcher.home.HomeController
import com.example.tgml_the_great_mobile_launcher.home.HomeItemType
import com.example.tgml_the_great_mobile_launcher.toSafeImageBitmap
import kotlin.math.floor

private const val HOME_COLUMNS = 5
private const val HOME_ROWS = 6

@Composable
	fun HomeScreen(
	apps: List<AppInfo>,
	homeController: HomeController,
	onAppClick: (AppInfo) -> Unit,
	backgroundUri: Uri?,
	page: Int,
	pageCount: Int,
	transitionStyle: PageTransitionStyle,
	onPageSelected: (Int) -> Unit,
	onGesture: (GestureType) -> Unit,
	layout: LauncherLayout,
	onAddHomeScreen: () -> Unit,
	onPickBackground: () -> Unit,
				onLayoutChange: (LauncherLayout) -> Unit,
	onTransitionStyleChange: (PageTransitionStyle) -> Unit
) {
	var editMode by remember { mutableStateOf(false) }
	var settingsOpen by remember { mutableStateOf(false) }
	var draggedItemId by remember { mutableStateOf<String?>(null) }
	var appPickerOpen by remember { mutableStateOf(false) }

	val launcherScale by animateFloatAsState(
		targetValue = if (editMode) 0.88f else 1f,
		animationSpec = tween(300),
		label = "homeLauncherScale"
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.pointerInput(Unit) {
				detectTapGestures(onLongPress = { editMode = true })
			}
	) {
		if (editMode) {
			LauncherBackground(backgroundUri = backgroundUri, blurred = true)
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.graphicsLayer {
					scaleX = launcherScale
					scaleY = launcherScale
				}
				.detectLauncherSwipes(
					verticalOnly = true,
					onSwipe = onGesture
				)
				.detectHorizontalPageSwipes(onSwipe = onGesture)
		) {
			AnimatedPageContent(
				page = page,
				style = transitionStyle,
				modifier = Modifier.fillMaxSize()
			) { targetPage ->
				Box(Modifier.fillMaxSize()) {
					LauncherBackground(backgroundUri = backgroundUri)

					BoxWithConstraints(
						modifier = Modifier
							.fillMaxSize()
							.padding(bottom = 96.dp)
					) {
						val cellWidth = maxWidth / HOME_COLUMNS
						val cellHeight = maxHeight / HOME_ROWS
						val density = LocalDensity.current
						val cellWidthPx = with(density) { cellWidth.toPx() }
						val cellHeightPx = with(density) { cellHeight.toPx() }

						homeController.itemsOnPage(targetPage)
							.filter { it.type == HomeItemType.APP_SHORTCUT }
							.forEach { item ->
								val app = apps.find {
									it.packageName == item.packageName &&
											it.activityName == item.activityName
								} ?: return@forEach

								var dragOffset by remember(item.id) {
									mutableStateOf(Offset.Zero)
								}

								Box(
									modifier = Modifier
										.offset(
											x = cellWidth * item.cellX +
													with(density) { dragOffset.x.toDp() },
											y = cellHeight * item.cellY +
													with(density) { dragOffset.y.toDp() }
										)
										.size(
											width = cellWidth,
											height = cellHeight
										)
										.pointerInput(
											item.id,
											editMode,
											targetPage,
											cellWidthPx,
											cellHeightPx
										) {
											detectDragGesturesAfterLongPress(
												onDragStart = {
													editMode = true
												},
												onDrag = { change, amount ->
													change.consume()
													dragOffset += amount
												},
												onDragEnd = {
													val startX = item.cellX * cellWidthPx
													val startY = item.cellY * cellHeightPx

													val targetX = floor(
														(startX + dragOffset.x +
																cellWidthPx / 2f) /
																cellWidthPx
													).toInt().coerceIn(0, HOME_COLUMNS - 1)

													val targetY = floor(
														(startY + dragOffset.y +
																cellHeightPx / 2f) /
																cellHeightPx
													).toInt().coerceIn(0, HOME_ROWS - 1)

													homeController.moveItem(
														itemId = item.id,
														page = targetPage,
														cellX = targetX,
														cellY = targetY
													)
													dragOffset = Offset.Zero
												},
												onDragCancel = {
													dragOffset = Offset.Zero
												}
											)
										}
										.clickable(enabled = !editMode) {
											onAppClick(app)
										},
									contentAlignment = Alignment.Center
								) {
									Column(
										horizontalAlignment = Alignment.CenterHorizontally,
										verticalArrangement = Arrangement.Center,
										modifier = Modifier.fillMaxSize()
									) {
										Image(
											bitmap = remember(app.packageName, app.activityName) {
												app.icon.toSafeImageBitmap(96, 96)
											},
											contentDescription = app.name,
											contentScale = ContentScale.Fit,
											modifier = Modifier.size(42.dp)
										)

										Text(
											text = app.name,
											color = Color.White,
											fontSize = 11.sp,
											maxLines = 2,
											modifier = Modifier.padding(
												top = 4.dp,
												start = 2.dp,
												end = 2.dp
											)
										)
									}

									if (editMode) {
										Box(
											modifier = Modifier
												.align(Alignment.TopEnd)
												.padding(3.dp)
												.size(22.dp)
												.background(
													Color.Red,
													CircleShape
												)
												.clickable {
													homeController.removeItem(item.id)
												},
											contentAlignment = Alignment.Center
										) {
											Text(
												text = "×",
												color = Color.White,
												fontSize = 17.sp
											)
										}
									}
								}
							}
					}

					Column(
						modifier = Modifier
							.align(Alignment.BottomCenter)
							.padding(bottom = 12.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						PageIndicator(
							page = targetPage,
							pageCount = pageCount,
							onPageSelected = onPageSelected,
							modifier = Modifier.padding(bottom = 8.dp)
						)

						Text(
							text = "Swipe up to open apps",
							color = Color.White,
							fontSize = 14.sp
						)
					}
				}
			}

			if (!editMode) {
				Text(
					text = "Edit",
					color = Color.White,
					fontSize = 13.sp,
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.padding(end = 18.dp, bottom = 22.dp)
						.background(
							Color.Black.copy(alpha = 0.65f),
							RoundedCornerShape(18.dp)
						)
						.clickable { editMode = true }
						.padding(horizontal = 14.dp, vertical = 8.dp)
				)
			}
		}

		AnimatedVisibility(
			visible = editMode,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.navigationBarsPadding(),
			enter = fadeIn(tween(200)) +
					slideInVertically(
						initialOffsetY = { it },
						animationSpec = tween(300)
					),
			exit = fadeOut(tween(200)) +
					slideOutVertically(
						targetOffsetY = { it },
						animationSpec = tween(300)
					)
		) {
			EditorBar(
				onBackgroundClick = onPickBackground,
				onAddScreen = onAddHomeScreen,
				onSettingsClick = { settingsOpen = true },
				onDone = { editMode = false },
				onAddAppClick = { appPickerOpen = true }
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
				currentTransitionStyle = transitionStyle,
				onTransitionStyleChange = onTransitionStyleChange,
				onClose = { settingsOpen = false }
			)
		}

		if (appPickerOpen) {
			AlertDialog(
				onDismissRequest = { appPickerOpen = false },
				title = { Text("Add app to home screen") },
				text = {
					LazyColumn {
						items(
							items = apps,
							key = { "${it.packageName}/${it.activityName}" }
						) { app ->
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.clickable {
										val added = homeController.addAppShortcut(
											packageName = app.packageName,
											activityName = app.activityName,
											page = page
										)
										if (added) appPickerOpen = false
									}
									.padding(vertical = 10.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								Image(
									bitmap = remember(app.packageName, app.activityName) {
										app.icon.toSafeImageBitmap(72, 72)
									},
									contentDescription = app.name,
									modifier = Modifier.size(36.dp)
								)

								Text(
									text = app.name,
									modifier = Modifier.padding(start = 12.dp)
								)
							}
						}
					}
				},
				confirmButton = {
					TextButton(onClick = { appPickerOpen = false }) {
						Text("Close")
					}
				}
			)
		}
	}
}
