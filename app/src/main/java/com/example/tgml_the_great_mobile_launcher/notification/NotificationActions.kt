package com.example.tgml_the_great_mobile_launcher.notification

import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle

object TgmlNotificationActions {

	fun open(context: Context, item: TgmlNotification) {
		val launched = try {
			val intent = item.contentIntent
			intent?.send()
			intent != null
		} catch (_: PendingIntent.CanceledException) {
			false
		}

		if (!launched) {
			context.packageManager.getLaunchIntentForPackage(item.packageName)?.let {
				context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
			}
		}

		if (item.autoCancel) TgmlNotificationListener.dismiss(item.key)
	}

	fun trigger(action: TgmlNotificationAction): Boolean = try {
		action.action.actionIntent?.send()
		action.action.actionIntent != null
	} catch (_: PendingIntent.CanceledException) {
		false
	}

	fun reply(context: Context, action: TgmlNotificationAction, text: String): Boolean {
		val inputs = action.action.remoteInputs?.takeIf { it.isNotEmpty() } ?: return false
		val pending = action.action.actionIntent ?: return false

		val fillIn = Intent()
		val results = Bundle()
		inputs.forEach { results.putCharSequence(it.resultKey, text) }
		RemoteInput.addResultsToIntent(inputs, fillIn, results)

		return try {
			pending.send(context, 0, fillIn)
			true
		} catch (_: PendingIntent.CanceledException) {
			false
		}
	}
}
