package com.example.tgml_the_great_mobile_launcher.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgml_the_great_mobile_launcher.gestures.GestureType
import com.example.tgml_the_great_mobile_launcher.gestures.detectLauncherSwipes

@Composable
fun HomeScreen(
    backgroundUri: Uri?,
    page: Int,
    pageCount: Int,
    transitionStyle: PageTransitionStyle,
    onPageSelected: (Int) -> Unit,
    onGesture: (GestureType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .detectLauncherSwipes(onSwipe = onGesture)
    ) {
        AnimatedPageContent(page = page, style = transitionStyle, modifier = Modifier.fillMaxSize()) { targetPage ->
            Box(Modifier.fillMaxSize()) {
                LauncherBackground(backgroundUri = backgroundUri)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PageIndicator(
                        page = targetPage,
                        pageCount = pageCount,
                        onPageSelected = onPageSelected,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Swipe up to open apps",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
