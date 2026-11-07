package com.example.tgml_the_great_mobile_launcher.home.widgets

import com.example.tgml_the_great_mobile_launcher.home.HomeItem
import com.example.tgml_the_great_mobile_launcher.home.HomePlacementEngine

object WidgetResizeHandler {
    fun resized(
        item: HomeItem,
        deltaX: Int,
        deltaY: Int,
        grid: HomePlacementEngine.GridSize
    ): Pair<Int, Int>? {
        if (item.widgetId == null) return null
        val nextX = (item.spanX + deltaX).coerceIn(1, grid.columns - item.cellX)
        val nextY = (item.spanY + deltaY).coerceIn(1, grid.rows - item.cellY)
        if (nextX == item.spanX && nextY == item.spanY) return null
        return nextX to nextY
    }
}
