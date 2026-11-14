package com.example.tgml_the_great_mobile_launcher

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
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
	private val folderRadius by lazy { dp(126).toFloat() }
	private val appRadius by lazy { dp(292).toFloat() }
	private val appCache = mutableMapOf<String, List<AppInfo>>()

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
			gravity = Gravity.LEFT or Gravity.TOP
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
						}
						down = false
						return true
					}
					MotionEvent.ACTION_CANCEL -> { down = false; return true }
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
			gravity = Gravity.LEFT or Gravity.TOP
			x = (centerX - size / 2f).toInt().coerceAtLeast(0)
			y = (centerY - size / 2f).toInt().coerceAtLeast(0)
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
		if (hoveredFolder != null && hoveredFolder != activeFolder) transitionToFolder(hoveredFolder, false)
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
		val intent = packageManager.getLaunchIntentForPackage(app.packageName) ?: return
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
	}

	private fun loadApps(folder: SideHandleFolder): List<AppInfo> {
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

	private fun radialPoint(radius: Float, index: Int, count: Int, centerY: Float): Pair<Float, Float> {
		val angle = (if (count <= 1) 0.0 else -82.0 + 164.0 * index / (count - 1)) * PI / 180.0
		return Pair(cos(angle).toFloat() * radius, centerY + sin(angle).toFloat() * radius)
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
			val centerX = 0f
			val centerY = height / 2f
			paint.color = 0xDD20242C.toInt()
			canvas.drawRoundRect(RectF(0f, centerY - 42f * density, 30f * density, centerY + 42f * density), 18f * density, 18f * density, paint)
			paint.color = android.graphics.Color.WHITE
			canvas.drawCircle(15f * density, centerY, 3.5f * density, paint)

			if (!expanded && !homeExpanded) return
			val folderRadius = 126f * density
			val appRadius = 292f * density
			paint.style = Paint.Style.STROKE
			paint.strokeWidth = 2f * density
			paint.color = 0xAAFFFFFF.toInt()
			canvas.drawCircle(centerX, centerY, folderRadius, paint)
			if (activeFolder in SideHandlePreferences.loadFolders(this@SideHandleService).indices) {
				paint.color = (0x55 * transitionProgress).toInt() shl 24 or 0x00FFFFFF
				canvas.drawCircle(centerX, centerY, appRadius, paint)
			}
			paint.style = Paint.Style.FILL

			val folders = SideHandlePreferences.loadFolders(this@SideHandleService)
			folders.forEachIndexed { index, folder ->
				val point = radialPoint(folderRadius, index, folders.size, centerY)
				paint.color = if (index == activeFolder) 0xFF536DFE.toInt() else 0xEE252B36.toInt()
				canvas.drawCircle(point.first, point.second, 28f * density, paint)
				paint.color = android.graphics.Color.WHITE
				paint.textSize = 10f * density
				paint.textAlign = Paint.Align.CENTER
				canvas.drawText(folder.name.take(9), point.first, point.second + 45f * density, paint)
				paint.textSize = 22f * density
				canvas.drawText(":D", point.first, point.second + 8f * density, paint)
			}

			if (activeFolder in folders.indices) {
				if (previousFolder in folders.indices && previousFolder != activeFolder && transitionProgress < 1f) {
					drawApps(canvas, folders[previousFolder], centerX, centerY, 0.18f * (1f - transitionProgress), 0.92f)
				}
				drawApps(canvas, folders[activeFolder], centerX, centerY, transitionProgress, 0.82f + 0.18f * transitionProgress)
			}
		}

		private fun drawApps(canvas: Canvas, folder: SideHandleFolder, cx: Float, cy: Float, alpha: Float, scale: Float) {
			val apps = loadApps(folder)
			apps.forEachIndexed { index, app ->
				val point = radialPoint(292f * density * scale, index, apps.size, cy)
				paint.color = ((0xEE * alpha).toInt().coerceIn(0, 255) shl 24) or 0x00171B22
				canvas.drawCircle(point.first, point.second, 26f * density * scale, paint)
				val oldAlpha = paint.alpha
				paint.alpha = (255 * alpha).toInt().coerceIn(0, 255)
				drawIcon(canvas, app.icon, point.first, point.second, alpha)
				paint.alpha = oldAlpha
				paint.color = (android.graphics.Color.WHITE and 0x00FFFFFF) or ((255 * alpha).toInt().coerceIn(0, 255) shl 24)
				paint.textSize = 10f * density
				paint.textAlign = Paint.Align.CENTER
				canvas.drawText(app.name.take(11), point.first, point.second + 39f * density * scale, paint)
			}
		}

		private fun drawIcon(canvas: Canvas, icon: Drawable, x: Float, y: Float, alpha: Float) {
			val bounds = (iconSize / 2f).toInt()
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
					lastX = event.x
					lastY = event.y
					if (!homeExpanded) postDelayed(longPressRunnable, 350L)
					return true
				}
				MotionEvent.ACTION_MOVE -> {
					if (!pointerDown) return false
					lastX = event.x
					lastY = event.y
					if (expanded) {
						updateHover(event.x, event.y)
						visualView.invalidate()
					}
					return true
				}
				MotionEvent.ACTION_UP -> {
					if (!pointerDown) return false
					pointerDown = false
					removeCallbacks(longPressRunnable)
					if (longPressTriggered) {
						val app = getHoveredApp(event.x, event.y)
						if (app != null) launchApp(app)
					}
					if (!homeExpanded) collapse()
					return true
				}
				MotionEvent.ACTION_CANCEL -> {
					pointerDown = false
					removeCallbacks(longPressRunnable)
					if (!homeExpanded) collapse()
					return true
				}
			}
			return true
		}

		private fun updateHover(x: Float, y: Float) {
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