
package com.example.tgml_the_great_mobile_launcher.home.widgets

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun WidgetPicker(
	providers: List<AppWidgetProviderInfo>,
	onWidgetSelected: (AppWidgetProviderInfo) -> Unit,
	onDismiss: () -> Unit
) {
	val packageManager = LocalContext.current.packageManager

	val sortedProviders = providers.sortedBy {
		it.loadLabel(packageManager).toString().lowercase()
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
				LazyColumn {
					items(
						items = sortedProviders,
						key = { it.provider.flattenToString() }
					) { provider ->
						Column(
							modifier = Modifier
								.fillMaxWidth()
								.clickable {
									onWidgetSelected(provider)
								}
								.padding(vertical = 12.dp)
						) {
							Text(
								text = provider.loadLabel(packageManager).toString()
							)

							Text(
								text = provider.provider.packageName,
								modifier = Modifier.padding(top = 3.dp),
								color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
							)

							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(top = 5.dp),
								horizontalArrangement = Arrangement.SpaceBetween,
								verticalAlignment = Alignment.CenterVertically
							) {
								Text(
									text = "Minimum size: ${provider.minWidth} × ${provider.minHeight} dp",
									color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
									style = androidx.compose.material3.MaterialTheme.typography.bodySmall
								)
							}
						}

						HorizontalDivider()
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
