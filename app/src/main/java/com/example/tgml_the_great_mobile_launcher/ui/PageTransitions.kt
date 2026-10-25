package com.example.tgml_the_great_mobile_launcher.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Available effects for changing launcher pages. */
enum class PageTransitionStyle {
    SLIDE,
    FADE,
    SLIDE_FADE
}

@Composable
fun AnimatedPageContent(
    page: Int,
    style: PageTransitionStyle,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    AnimatedContent(
        targetState = page,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState > initialState) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            when (style) {
                PageTransitionStyle.SLIDE -> slideIntoContainer(direction, tween(280)) togetherWith
                    slideOutOfContainer(direction, tween(280))
                PageTransitionStyle.FADE -> fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                PageTransitionStyle.SLIDE_FADE -> (slideIntoContainer(direction, tween(280)) + fadeIn(tween(220))) togetherWith
                    (slideOutOfContainer(direction, tween(280)) + fadeOut(tween(180)))
            }
        },
        label = "launcherPageTransition"
    ) { targetPage -> content(targetPage) }
}

@Composable
fun PageIndicator(
    page: Int,
    pageCount: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == page) 8.dp else 6.dp)
                    .background(
                        if (index == page) Color.White else Color.White.copy(alpha = 0.45f),
                        CircleShape
                    )
                    .clickable { onPageSelected(index) }
            )
        }
    }
}
