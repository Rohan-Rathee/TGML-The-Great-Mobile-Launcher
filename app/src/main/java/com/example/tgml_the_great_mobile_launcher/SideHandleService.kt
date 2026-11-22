
package com.example.tgml_the_great_mobile_launcher

import android.animation.ValueAnimator
import com.example.tgml_the_great_mobile_launcher.privacy.AppLockGateActivity
import com.example.tgml_the_great_mobile_launcher.privacy.AppPrivacyPreferences
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SideHandleService : Service() {
	companion object {
		const val ACTION_HOME_MODE = "com.example.tgml_the_great_mobile_launcher.SIDE_HANDLE_HOME_MODE"
		const val ACTION_REFRESH = "com.example.tgml_the_great_mobile_launcher.SIDE_HANDLE_REFRESH"
		const val EXTRA_HOME_MODE = "home_mode"
		private const val CHANNEL_ID = "tgml_side_handle"
		private const val NOTIFICATION_ID = 7412

		fun start(context: android.content.Context) {
			ContextCompat.startForegroundService(context, Intent(context, SideHandleService::class.java))
		}

		fun stop(context: android.content.Context) {
			context.stopService(Intent(context, SideHandleService::class.java))
		}
	}

	private lateinit var windowManager: WindowManager
	private lateinit var visualView: SideHandleVisualView
	private lateinit var handleTouchView: HandleTouchView
	private lateinit var visualParams: WindowManager.LayoutParams
	private lateinit var handleParams: WindowManager.LayoutParams
	private val targetViews = mutableListOf<View>()
	private var homeMode = false
	private var expanded = false
	private var homeExpanded = false
	private var activeFolder = -1
	private var previousFolder = -1
	private var transitionProgress = 1f
	private var transitionAnimator: ValueAnimator? = null
	private val handleWidth by lazy { dp(44) }
	private val expandedWidth by lazy { dp(340) }
	private val folderRadius by lazy { dp(120).toFloat() }
	private val appRadius by lazy { dp(240).toFloat() }
	private val appCache = mutableMapOf<String, List<AppInfo>>()
	private var pointerX = 0f
	private var pointerY = 0f
	private var pointerActive = false

	override fun onCreate() {
		super.onCreate()
		createNotificationChannel()
		if (Build.VERSION.SDK_INT >= 34) {
			startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
		} else {
			startForeground(NOTIFICATION_ID, buildNotification())
		}

		windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
		visualView = SideHandleVisualView()
		visualParams = WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.MATCH_PARENT,
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			PixelFormat.TRANSLUCENT
		).apply {
			gravity = Gravity.LEFT or Gravity.TOP
		}

		handleTouchView = HandleTouchView()
		handleParams = WindowManager.LayoutParams(
			handleWidth,
			WindowManager.LayoutParams.MATCH_PARENT,
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
			PixelFormat.TRANSLUCENT
		).apply {
			gravity = sideGravity()
		}

		try {
			windowManager.addView(visualView, visualParams)
			windowManager.addView(handleTouchView, handleParams)
		} catch (_: SecurityException) {
			stopForeground(STOP_FOREGROUND_REMOVE)
			stopSelf()
		}
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when (intent?.action) {
			ACTION_HOME_MODE -> {
				homeMode = intent.getBooleanExtra(EXTRA_HOME_MODE, false)
				homeExpanded = homeMode && SideHandlePreferences.homeIntegration(this)
				if (!homeExpanded) {
					activeFolder = -1
					previousFolder = -1
				}
				updateWindow()
			}
			ACTION_REFRESH -> {
				appCache.clear()
				updateWindow()
			}
		}
		return START_STICKY
	}

	private fun updateWindow() {
		if (!::handleTouchView.isInitialized) return
		handleParams.width = if (expanded) expandedWidth else handleWidth
		handleParams.gravity = sideGravity()
		try {
			windowManager.updateViewLayout(handleTouchView, handleParams)
		} catch (_: Exception) { }
		visualView.invalidate()
		updateHomeTargets()
	}

	private fun collapse() {
		expanded = false
		activeFolder = -1
		previousFolder = -1
		pointerActive = false
		transitionProgress = 1f
		updateWindow()
	}

	private fun updateHomeTargets() {
		if (!::visualView.isInitialized) return
		targetViews.forEach { view ->
			try { windowManager.removeView(view) } catch (_: Exception) { }
		}
		targetViews.clear()
		if (!homeExpanded) return

		val centerY = resources.displayMetrics.heightPixels / 2f
		val folders = SideHandlePreferences.loadFolders(this)
		folders.forEachIndexed { index, folder ->
			val point = radialPoint(folderRadius, index, folders.size, centerY)
			addTouchTarget(point.first, point.second, onClick = {
				val selectedApp = getHoveredHomeApp(homePointerX, homePointerY)
				if (selectedApp != null) launchApp(selectedApp)
				updateHomeTargets()
			}, onMove = { x, y -> updateHomeHover(x, y) })
		}

		if (activeFolder in folders.indices) {
			val apps = loadApps(folders[activeFolder])
			apps.forEachIndexed { index, app ->
				val point = radialPoint(appRadius * (0.82f + 0.18f * transitionProgress), index, apps.size, centerY)
				addTouchTarget(point.first, point.second, onClick = {
					getHoveredHomeApp(homePointerX, homePointerY)?.let { launchApp(it) } ?: launchApp(app)
					updateHomeTargets()
				}, onMove = { x, y -> updateHomeHover(x, y) })
			}
		}
	}

	private var homePointerX = 0f
	private var homePointerY = 0f

	private fun addTouchTarget(centerX: Float, centerY: Float, onClick: () -> Unit, onMove: ((Float, Float) -> Unit)? = null) {
		val size = dp(64)
		val target = object : View(this) {
			private var down = false
			override fun onTouchEvent(event: MotionEvent): Boolean {
				when (event.actionMasked) {
					MotionEvent.ACTION_DOWN -> {
						down = true
						homePointerX = event.rawX
						homePointerY = event.rawY
						onMove?.invoke(homePointerX, homePointerY)
						return true
					}
					MotionEvent.ACTION_MOVE -> {
						homePointerX = event.rawX
						homePointerY = event.rawY
						onMove?.invoke(homePointerX, homePointerY)
						return true
					}
					MotionEvent.ACTION_UP -> {
						if (down) {
							homePointerX = event.rawX
							homePointerY = event.rawY
							onMove?.invoke(homePointerX, homePointerY)
							onClick()
							pointerActive = false
							visualView.invalidate()
						}
						down = false
						return true
					}
					MotionEvent.ACTION_CANCEL -> {
						down = false
						pointerActive = false
						visualView.invalidate()
						return true
					}
				}
				return true
			}
		}
		val targetParams = WindowManager.LayoutParams(
			size,
			size,
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
			PixelFormat.TRANSLUCENT
		).apply {
			gravity = sideGravity()
			x = if (isRightSide()) {
				(resources.displayMetrics.widthPixels - centerX - size / 2f).toInt()
			} else {
				(centerX - size / 2f).toInt()
			}
			y = (centerY - size / 2f).toInt()
		}
		try {
			windowManager.addView(target, targetParams)
			targetViews.add(target)
		} catch (_: Exception) { }
	}

	private fun updateHomeHover(x: Float, y: Float) {
		homePointerX = x
		homePointerY = y
		val folders = SideHandlePreferences.loadFolders(this)
		val centerY = resources.displayMetrics.heightPixels / 2f
		val hoveredFolder = folders.indices.firstOrNull { index ->
			val point = radialPoint(folderRadius, index, folders.size, centerY)
			(x - point.first) * (x - point.first) + (y - point.second) * (y - point.second) <= dp(72).toFloat() * dp(72).toFloat()
		}
		if (hoveredFolder != null && hoveredFolder != activeFolder) transitionToFolder(hoveredFolder)
		visualView.invalidate()
	}

	private fun getHoveredHomeApp(x: Float, y: Float): AppInfo? {
		val folders = SideHandlePreferences.loadFolders(this)
		if (activeFolder !in folders.indices) return null
		val apps = loadApps(folders[activeFolder])
		val centerY = resources.displayMetrics.heightPixels / 2f
		return apps.firstOrNullIndexed { index, _ ->
			val point = radialPoint(appRadius * (0.82f + 0.18f * transitionProgress), index, apps.size, centerY)
			(x - point.first) * (x - point.first) + (y - point.second) * (y - point.second) <= dp(68).toFloat() * dp(68).toFloat()
		}
	}

	private inline fun <T> Iterable<T>.firstOrNullIndexed(predicate: (Int, T) -> Boolean): T? {
		forEachIndexed { index, item -> if (predicate(index, item)) return item }
		return null
	}

	private fun selectFolder(index: Int) {
		if (index !in SideHandlePreferences.loadFolders(this).indices) return
		if (activeFolder != index) {
			previousFolder = activeFolder
			activeFolder = index
			transitionAnimator?.cancel()
			transitionProgress = 0f
			transitionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
				duration = 220L
				addUpdateListener {
					transitionProgress = it.animatedValue as Float
					visualView.invalidate()
				}
				start()
			}
			updateWindow()
		}
	}

	private fun launchApp(app: AppInfo) {
		if (AppPrivacyPreferences.isLocked(this, app.packageName)) {
			startActivity(AppLockGateActivity.intent(this, app.packageName, app.activityName))
			return
		}
		val intent = packageManager.getLaunchIntentForPackage(app.packageName) ?: return
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
	}

	private fun loadApps(folder: SideHandleFolder): List<AppInfo> =
		loadAppsUnfiltered(folder).filterNot { AppPrivacyPreferences.isHidden(this, it.packageName) }

	private fun loadAppsUnfiltered(folder: SideHandleFolder): List<AppInfo> {
		val key = folder.packages.joinToString("|")
		return appCache.getOrPut(key) {
			folder.packages.mapNotNull { packageName ->
				try {
					val info = packageManager.getApplicationInfo(packageName, 0)
					val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return@mapNotNull null
					AppInfo(packageManager.getApplicationLabel(info).toString(), packageName, intent.component?.className ?: return@mapNotNull null, packageManager.getApplicationIcon(packageName))
				} catch (_: Exception) { null }
			}
		}
	}

	private fun isRightSide() = SideHandlePreferences.isRightSide(this)

	private fun sideCenterX() = if (isRightSide()) resources.displayMetrics.widthPixels.toFloat() else 0f

	private fun sideGravity() = (if (isRightSide()) Gravity.RIGHT else Gravity.LEFT) or Gravity.TOP

	private fun radialPoint(radius: Float, index: Int, count: Int, centerY: Float): Pair<Float, Float> {
		val angle = (if (count <= 1) 0.0 else -82.0 + 164.0 * index / (count - 1)) * PI / 180.0
		val direction = if (SideHandlePreferences.isRightSide(this)) -1f else 1f
		val offsetX = if (direction < 0f) resources.displayMetrics.widthPixels.toFloat() else 0f
		val x = direction * cos(angle).toFloat() * radius + offsetX
		val y = centerY + sin(angle).toFloat() * radius
		return Pair(x, y)
	}

	private fun createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(CHANNEL_ID, "Side handle", NotificationManager.IMPORTANCE_LOW)
			getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
		}
	}

	private fun buildNotification(): Notification {
		val openIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
		return NotificationCompat.Builder(this, CHANNEL_ID)
			.setSmallIcon(android.R.drawable.ic_menu_view)
			.setContentTitle("TGML side handle is active")
			.setContentText("Hold the edge handle to open your radial app launcher")
			.setContentIntent(openIntent)
			.setOngoing(true)
			.build()
	}

	private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

	private fun transitionToFolder(index: Int, refreshTargets: Boolean = true) {
		if (index !in SideHandlePreferences.loadFolders(this).indices || index == activeFolder) return
		previousFolder = activeFolder
		activeFolder = index
		transitionAnimator?.cancel()
		transitionProgress = 0f
		transitionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
			duration = 220L
			addUpdateListener {
				transitionProgress = it.animatedValue as Float
				visualView.invalidate()
			}
			start()
		}
		if (refreshTargets) updateHomeTargets()
		visualView.invalidate()
	}

	private inner class SideHandleVisualView : View(this@SideHandleService) {
		private val density = resources.displayMetrics.density
		private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
		private val iconSize = 42f * density

		override fun onDraw(canvas: Canvas) {
			super.onDraw(canvas)
			val rightSide = SideHandlePreferences.isRightSide(this@SideHandleService)
			val centerX = if (rightSide) width.toFloat() else 0f
			val centerY = height / 2f

			paint.shader = null
			paint.style = Paint.Style.FILL
			paint.color = 0xDD20242C.toInt()
			val handleRect = if (isRightSide()) {
				RectF(width - 30f * density, centerY - 42f * density, width.toFloat(), centerY + 42f * density)
			} else {
				RectF(0f, centerY - 42f * density, 30f * density, centerY + 42f * density)
			}
			if (rightSide) {
				canvas.drawRoundRect(RectF(width - 30f * density, centerY - 42f * density, width.toFloat(), centerY + 42f * density), 18f * density, 18f * density, paint)
				paint.color = android.graphics.Color.WHITE
				canvas.drawCircle(width - 15f * density, centerY, 3.5f * density, paint)
			} else {
				canvas.drawRoundRect(RectF(0f, centerY - 42f * density, 30f * density, centerY + 42f * density), 18f * density, 18f * density, paint)
				paint.color = android.graphics.Color.WHITE
				canvas.drawCircle(15f * density, centerY, 3.5f * density, paint)
			}
			if (!expanded && !homeExpanded) return

			val currentFolders = SideHandlePreferences.loadFolders(this@SideHandleService)
			val folderColors = currentFolders.map { it.color }.toIntArray()
			drawGradientArcBand(canvas, centerX, centerY, folderRadius, folderColors)

			val folderColor = if (activeFolder in currentFolders.indices) {
				currentFolders[activeFolder].color
			} else {
				SideHandlePreferences.defaultArcColor(this@SideHandleService)
			}
			val previousColor = if (previousFolder in currentFolders.indices) {
				currentFolders[previousFolder].color
			} else {
				folderColor
			}
			val outerColor = blendColors(previousColor, folderColor, transitionProgress)

			currentFolders.forEachIndexed { index, folder ->
				val point = radialPoint(folderRadius, index, currentFolders.size, centerY)
				paint.shader = null
				paint.style = Paint.Style.FILL
				paint.color = if (index == activeFolder) 0xFF303846.toInt() else 0xEE252B36.toInt()
				canvas.drawCircle(point.first, point.second, 28f * density, paint)
				paint.color = android.graphics.Color.WHITE
				paint.textSize = 10f * density
				paint.textAlign = Paint.Align.CENTER
				paint.textSize = 22f * density
				paint.textAlign = Paint.Align.CENTER
				canvas.drawText("▣", point.first, point.second + 8f * density, paint)
			}

			if (activeFolder in currentFolders.indices) {
				val appScale = 0.82f + 0.18f * transitionProgress
				drawArcBand(canvas, centerX, centerY, appRadius * appScale, outerColor, transitionProgress)
				drawApps(canvas, currentFolders[activeFolder], centerX, centerY, transitionProgress, appScale)
			}
		}

		private fun drawGradientArcBand(canvas: Canvas, cx: Float, cy: Float, radius: Float, colors: IntArray) {
			if (colors.isEmpty()) return
			val gradientColors = if (colors.size == 1) intArrayOf(colors[0], colors[0]) else colors
			val endPosition = 164f / 360f
			val positions = FloatArray(gradientColors.size) { index ->
				if (gradientColors.size == 1) 0f else endPosition * index / (gradientColors.size - 1)
			}
			val shader = SweepGradient(cx, cy, gradientColors, positions)
			val rightSide = SideHandlePreferences.isRightSide(this@SideHandleService)
			val startAngle = if (rightSide) 98f else -82f

			val matrix = Matrix().apply { setRotate(startAngle, cx, cy) }
			shader.setLocalMatrix(matrix)

			paint.style = Paint.Style.STROKE
			paint.strokeCap = Paint.Cap.ROUND
			paint.strokeWidth = 56f * density
			paint.shader = shader
			paint.alpha = 255
			canvas.drawArc(RectF(cx - radius, cy - radius, cx + radius, cy + radius), startAngle, 164f, false, paint)
			paint.shader = null
			paint.style = Paint.Style.FILL
		}

		private fun drawArcBand(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, alpha: Float) {
			val oldStyle = paint.style
			val oldCap = paint.strokeCap
			val oldShader = paint.shader
			paint.shader = null
			val oldWidth = paint.strokeWidth

			paint.style = Paint.Style.STROKE
			paint.strokeCap = Paint.Cap.ROUND
			paint.strokeWidth = 56f * density
			paint.color = color
			paint.alpha = (255 * alpha).toInt().coerceIn(0, 255)
			val startAngle = if (SideHandlePreferences.isRightSide(this@SideHandleService)) 98f else -82f

			canvas.drawArc(
				RectF(cx - radius, cy - radius, cx + radius, cy + radius),
				startAngle,
				164f,
				false,
				paint
			)

			paint.style = oldStyle
			paint.shader = oldShader
			paint.strokeCap = oldCap
			paint.strokeWidth = oldWidth
			paint.alpha = 255
		}

		private fun blendColors(from: Int, to: Int, fraction: Float): Int {
			val progress = fraction.coerceIn(0f, 1f)
			return android.graphics.Color.argb(
				(android.graphics.Color.alpha(from) + (android.graphics.Color.alpha(to) - android.graphics.Color.alpha(from)) * progress).toInt(),
				(android.graphics.Color.red(from) + (android.graphics.Color.red(to) - android.graphics.Color.red(from)) * progress).toInt(),
				(android.graphics.Color.green(from) + (android.graphics.Color.green(to) - android.graphics.Color.green(from)) * progress).toInt(),
				(android.graphics.Color.blue(from) + (android.graphics.Color.blue(to) - android.graphics.Color.blue(from)) * progress).toInt()
			)
		}

		private fun drawApps(canvas: Canvas, folder: SideHandleFolder, cx: Float, cy: Float, alpha: Float, scale: Float) {
			val apps = loadApps(folder)
			apps.forEachIndexed { index, app ->
				val point = radialPoint(appRadius * scale, index, apps.size, cy)

				val dx = pointerX - point.first
				val dy = pointerY - point.second
				val distance = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()

				val proximity = if (pointerActive) {
					(1f - distance / (dp(150).toFloat())).coerceIn(0f, 1f)
				} else {
					0f
				}

				val easedProximity = proximity * proximity * (3f - 2f * proximity)
				val zoom = 1f + 0.65f * easedProximity

				paint.color = ((0xEE * alpha).toInt().coerceIn(0, 255) shl 24) or 0x00171B22
				canvas.drawCircle(point.first, point.second, 26f * density * scale * zoom, paint)

				val oldAlpha = paint.alpha
				paint.alpha = (255 * alpha).toInt().coerceIn(0, 255)
				drawIcon(canvas, app.icon, point.first, point.second, alpha, scale * zoom)
				paint.alpha = oldAlpha

				paint.color = (android.graphics.Color.WHITE and 0x00FFFFFF) or ((255 * alpha).toInt().coerceIn(0, 255) shl 24)
				paint.textSize = 10f * density
				paint.textAlign = Paint.Align.CENTER
			}
		}

		private fun drawIcon(canvas: Canvas, icon: Drawable, x: Float, y: Float, alpha: Float, scale: Float) {
			val bounds = (iconSize / 2f * scale).toInt()
			val oldAlpha = icon.alpha
			icon.alpha = (255 * alpha).toInt().coerceIn(0, 255)
			icon.setBounds((x - bounds).toInt(), (y - bounds).toInt(), (x + bounds).toInt(), (y + bounds).toInt())
			icon.draw(canvas)
			icon.alpha = oldAlpha
		}
	}

	private inner class HandleTouchView : View(this@SideHandleService) {
		private var pointerDown = false
		private var longPressTriggered = false
		private var lastX = 0f
		private var lastY = 0f
		private val longPressRunnable = Runnable {
			if (pointerDown && !homeExpanded) {
				pointerX = lastX
				pointerY = lastY
				pointerActive = true
				longPressTriggered = true
				activeFolder = -1
				previousFolder = -1
				transitionProgress = 1f
				expanded = true
				updateWindow()
				updateHover(lastX, lastY)
				visualView.invalidate()
			}
		}

		override fun onTouchEvent(event: MotionEvent): Boolean {
			val centerY = resources.displayMetrics.heightPixels / 2f
			when (event.actionMasked) {
				MotionEvent.ACTION_DOWN -> {
					pointerDown = true
					longPressTriggered = false
					lastX = event.rawX
					lastY = event.rawY
					if (!homeExpanded) postDelayed(longPressRunnable, 350L)
					return true
				}
				MotionEvent.ACTION_MOVE -> {
					if (!pointerDown) return false
					lastX = event.rawX
					lastY = event.rawY
					if (expanded) {
						updateHover(event.rawX, event.rawY)
						visualView.invalidate()
					}
					return true
				}
				MotionEvent.ACTION_UP -> {
					if (!pointerDown) return false
					pointerDown = false
					removeCallbacks(longPressRunnable)
					if (longPressTriggered) {
						val app = getHoveredApp(event.rawX, event.rawY)
						if (app != null) launchApp(app)
					}
					if (!homeExpanded) collapse()
					return true
				}
				MotionEvent.ACTION_CANCEL -> {
					pointerDown = false
					removeCallbacks(longPressRunnable)
					pointerActive = false
					if (!homeExpanded) collapse()
					visualView.invalidate()
					return true
				}
			}
			return true
		}

		private fun updateHover(x: Float, y: Float) {
			pointerX = x
			pointerY = y
			pointerActive = true

			val centerY = resources.displayMetrics.heightPixels / 2f
			val folders = SideHandlePreferences.loadFolders(this@SideHandleService)
			val hovered = folders.indices.firstOrNull { index ->
				val point = radialPoint(folderRadius, index, folders.size, centerY)
				(x - point.first) * (x - point.first) + (y - point.second) * (y - point.second) <= dp(72).toFloat() * dp(72).toFloat()
			}
			if (hovered != null && hovered != activeFolder) transitionToFolder(hovered)
		}

		private fun getHoveredApp(x: Float, y: Float): AppInfo? {
			val folders = SideHandlePreferences.loadFolders(this@SideHandleService)
			if (activeFolder !in folders.indices) return null
			val apps = loadApps(folders[activeFolder])
			val centerY = resources.displayMetrics.heightPixels / 2f
			return apps.firstOrNullIndexed { index, _ ->
				val point = radialPoint(appRadius * (0.82f + 0.18f * transitionProgress), index, apps.size, centerY)
				(x - point.first) * (x - point.first) + (y - point.second) * (y - point.second) <= dp(68).toFloat() * dp(68).toFloat()
			}
		}

		private inline fun <T> Iterable<T>.firstOrNullIndexed(predicate: (Int, T) -> Boolean): T? {
			forEachIndexed { index, item -> if (predicate(index, item)) return item }
			return null
		}
	}

	override fun onDestroy() {
		transitionAnimator?.cancel()
		targetViews.forEach { try { windowManager.removeView(it) } catch (_: Exception) { } }
		if (::handleTouchView.isInitialized) try { windowManager.removeView(handleTouchView) } catch (_: Exception) { }
		if (::visualView.isInitialized) try { windowManager.removeView(visualView) } catch (_: Exception) { }
		super.onDestroy()
	}

	override fun onBind(intent: Intent?): IBinder? = null
}
