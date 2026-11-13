
package com.example.tgml_the_great_mobile_launcher.home.widgets

import android.appwidget.AppWidgetProviderInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun WidgetPicker(
	providers: List<AppWidgetProviderInfo>,
	onWidgetSelected: (AppWidgetProviderInfo) -> Unit,
	onDismiss: () -> Unit
) {
	val context = LocalContext.current
	val packageManager = context.packageManager
	val density = context.resources.displayMetrics.densityDpi

	val sortedProviders = remember(providers) {
		providers.sortedBy {
			it.loadLabel(packageManager).toString().lowercase()
		}
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text("Choose a widget")
		},
		text = {
			if (sortedProviders.isEmpty()) {
				Text("No widgets are currently available.")
			} else {
				LazyVerticalGrid(
					columns = GridCells.Fixed(2),
					modifier = Modifier
						.fillMaxWidth()
						.height(460.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					items(
						items = sortedProviders,
						key = { it.provider.flattenToString() }
					) { provider ->

						val preview = remember(provider, context) {
							try {
								if (provider.previewImage != 0) {
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
										provider.loadPreviewImage(context, density)
									} else {
										packageManager.getDrawable(
											provider.provider.packageName,
											provider.previewImage,
											provider.activityInfo.applicationInfo
										)
									}
								} else {
									null
								}
							} catch (_: Exception) {
								null
							}
						}

						val appIcon = remember(provider, context) {
							try {
								packageManager.getApplicationIcon(provider.provider.packageName)
							} catch (_: Exception) {
								null
							}
						}

						val previewBitmap = remember(preview) {
							try {
								preview?.toBitmap(
									width = 480,
									height = 300
								)?.asImageBitmap()
							} catch (_: Exception) {
								null
							}
						}

						val iconBitmap = remember(appIcon) {
							try {
								appIcon?.toBitmap(
									width = 96,
									height = 96
								)?.asImageBitmap()
							} catch (_: Exception) {
								null
							}
						}

						Card(
							modifier = Modifier
								.fillMaxWidth()
								.clickable {
									onWidgetSelected(provider)
								},
							shape = RoundedCornerShape(14.dp),
							colors = CardDefaults.cardColors(
								containerColor = MaterialTheme.colorScheme.surfaceVariant
							)
						) {
							Column(
								modifier = Modifier.padding(8.dp),
								verticalArrangement = Arrangement.spacedBy(8.dp)
							) {
								Box(
									modifier = Modifier
										.fillMaxWidth()
										.height(130.dp)
										.background(
											MaterialTheme.colorScheme.surface,
											RoundedCornerShape(10.dp)
										),
									contentAlignment = Alignment.Center
								) {
									if (previewBitmap != null) {
										Image(
											bitmap = previewBitmap,
											contentDescription = null,
											modifier = Modifier
												.fillMaxSize()
												.padding(5.dp),
											contentScale = ContentScale.Fit
										)
									} else {
										Column(
											horizontalAlignment = Alignment.CenterHorizontally,
											verticalArrangement = Arrangement.spacedBy(6.dp)
										) {
											if (iconBitmap != null) {
												Image(
													bitmap = iconBitmap,
													contentDescription = null,
													modifier = Modifier.size(42.dp)
												)
											}

											Text(
												text = "Preview unavailable(R.I.P)",
												style = MaterialTheme.typography.labelSmall,
												color = MaterialTheme.colorScheme.onSurfaceVariant
											)
										}
									}
								}

								Row(
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.spacedBy(7.dp)
								) {
									if (iconBitmap != null) {
										Image(
											bitmap = iconBitmap,
											contentDescription = null,
											modifier = Modifier.size(28.dp)
										)
									}

									Column {
										Text(
											text = provider.loadLabel(packageManager).toString(),
											style = MaterialTheme.typography.bodyMedium,
											maxLines = 1
										)

										Text(
											text = "${provider.minWidth} × ${provider.minHeight} dp",
											style = MaterialTheme.typography.labelSmall,
											color = MaterialTheme.colorScheme.onSurfaceVariant
										)
									}
								}
							}
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text("Close")
			}
		}
	)
}
