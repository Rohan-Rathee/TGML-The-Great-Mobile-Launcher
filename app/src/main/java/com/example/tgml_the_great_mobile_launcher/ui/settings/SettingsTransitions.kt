
package com.example.tgml_the_great_mobile_launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tgml_the_great_mobile_launcher.ui.LayoutOption
import com.example.tgml_the_great_mobile_launcher.ui.PageTransitionStyle


@Composable
fun SettingsTransitions(
	currentTransitionStyle: PageTransitionStyle,
	onTransitionStyleChange: (PageTransitionStyle) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color(0xFF202020))
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(2.dp)
	) {
		LayoutOption(
			name = "Slide",
			selected = currentTransitionStyle == PageTransitionStyle.SLIDE,
			enabled = true,
			onClick = {
				onTransitionStyleChange(PageTransitionStyle.SLIDE)
			}
		)

		LayoutOption(
			name = "Fade",
			selected = currentTransitionStyle == PageTransitionStyle.FADE,
			enabled = true,
			onClick = {
				onTransitionStyleChange(PageTransitionStyle.FADE)
			}
		)

		LayoutOption(
			name = "Slide + Fade",
			selected = currentTransitionStyle == PageTransitionStyle.SLIDE_FADE,
			enabled = true,
			onClick = {
				onTransitionStyleChange(PageTransitionStyle.SLIDE_FADE)
			}
		)
	}
}
