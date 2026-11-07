import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tgml_the_great_mobile_launcher.home.widgets.WidgetHostController

@Composable
fun WidgetRenderer(
	widgetHost: WidgetHostController,
	widgetId: Int,
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current
	val density = LocalDensity.current

	val hostView = remember(widgetHost, widgetId) {
		widgetHost.createWidgetView(context, widgetId)
	}

	if (hostView != null) {
		AndroidView(
			factory = { hostView },
			modifier = modifier.onSizeChanged { size ->
				if (size.width > 0 && size.height > 0) {
					val widthDp = with(density) {
						size.width.toDp().value.toInt()
					}
					val heightDp = with(density) {
						size.height.toDp().value.toInt()
					}

					if (widthDp > 0 && heightDp > 0) {
						val options = Bundle().apply {
							putInt(
								AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
								widthDp
							)
							putInt(
								AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
								widthDp
							)
							putInt(
								AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
								heightDp
							)
							putInt(
								AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
								heightDp
							)
						}

						hostView.updateAppWidgetSize(
							options,
							widthDp,
							heightDp,
							widthDp,
							heightDp
						)
					}
				}
			},
			update = { it.requestLayout() }
		)
	} else {
		Box(
			modifier = modifier,
			contentAlignment = Alignment.Center
		) {
			Text("Widget unavailable")
		}
	}
}
