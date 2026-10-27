package com.example.tgml_the_great_mobile_launcher.ui

import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tgml_the_great_mobile_launcher.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun LauncherBackground(backgroundUri: Uri?, blurred: Boolean = false)
{
	val context = LocalContext.current
	val bitmap by produceState<android.graphics.Bitmap?> (
		initialValue = null,
		key1 = backgroundUri
	){
		value = if (backgroundUri == null){
			null    
		} else{
			withContext(Dispatchers.IO){
				try {
					context.contentResolver.openInputStream(backgroundUri)?.use { inputStream ->
						val options = BitmapFactory.Options().apply {
							inSampleSize = 2
						}
						BitmapFactory.decodeStream(inputStream, null, options)
					}
				} catch (_: Throwable){
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
			modifier = Modifier.fillMaxSize()
		)

	} else {

		Image(
			painter = painterResource(
				id = R.drawable.launcher_background
			),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxSize()
				.then(
					if (blurred) {
						Modifier.blur(35.dp)
					} else {
						Modifier
					}
				)
		)
	}
}

