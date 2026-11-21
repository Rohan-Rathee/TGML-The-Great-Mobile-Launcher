package com.example.tgml_the_great_mobile_launcher.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NotificationRepository {
	private val state = MutableStateFlow<List<TgmlNotification>>(emptyList())
	val notifications: StateFlow<List<TgmlNotification>> = state.asStateFlow()

	fun replaceAll(items: List<TgmlNotification>) {
		state.value = items.sortedByDescending { it.postTime }
	}

	fun upsert(item: TgmlNotification) {
		state.update { current ->
			(current.filterNot { it.key == item.key } + item).sortedByDescending { it.postTime }
		}
	}

	fun remove(key: String) {
		state.update { current -> current.filterNot { it.key == key } }
	}

	fun clear() {
		state.value = emptyList()
	}

	fun badgeCount(packageName: String, items: List<TgmlNotification> = state.value): Int =
		items.count { it.packageName == packageName && !it.isGroupSummary }
}
