
package com.example.tgml_the_great_mobile_launcher.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LauncherBackground(backgroundUri: android.net.Uri?, blurred: Boolean = false)
{
	val context = LocalContext.current

	val bitmap by produceState<Bitmap?>(initialValue = null, key1 = backgroundUri) {
		value = withContext(Dispatchers.IO) {
			if (backgroundUri == null) {
				null
			} else {
				try {
					context.contentResolver.openInputStream(backgroundUri)?.use {
						BitmapFactory.decodeStream(it)
					}
				} catch (_: Exception) {
					null
				}
			}
		}
	}

	if (bitmap != null) {
		Image(
			bitmap = bitmap!!.asImageBitmap(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxSize()
				.then(if (blurred) Modifier.blur(35.dp) else Modifier)
		)
	}
}
