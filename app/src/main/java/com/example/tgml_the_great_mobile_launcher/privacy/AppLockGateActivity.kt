package com.example.tgml_the_great_mobile_launcher.privacy

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class AppLockGateActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val packageName = intent.getStringExtra(EXTRA_PACKAGE)
		val activityName = intent.getStringExtra(EXTRA_ACTIVITY)
		if (packageName == null || activityName == null) {
			finish()
			return
		}

		if (!AppPrivacyPreferences.isLocked(this, packageName)) {
			openTarget(packageName, activityName)
			return
		}

		val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			BiometricManager.Authenticators.BIOMETRIC_STRONG or
					BiometricManager.Authenticators.DEVICE_CREDENTIAL
		} else {
			BiometricManager.Authenticators.BIOMETRIC_WEAK
		}

		if (BiometricManager.from(this).canAuthenticate(authenticators) !=
			BiometricManager.BIOMETRIC_SUCCESS
		) {
			Toast.makeText(
				this,
				"Set up a screen lock or fingerprint to use App Lock.",
				Toast.LENGTH_LONG
			).show()
			finish()
			return
		}

		val promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle("Unlock ${labelFor(packageName)}")
			.setAllowedAuthenticators(authenticators)
			.apply {
				if ((authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL) == 0) {
					setNegativeButtonText("Cancel")
				}
			}
			.build()

		val prompt = BiometricPrompt(
			this,
			ContextCompat.getMainExecutor(this),
			object : BiometricPrompt.AuthenticationCallback() {
				override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
					openTarget(packageName, activityName)
				}

				override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
					finish()
				}
			}
		)
		prompt.authenticate(promptInfo)
	}

	private fun openTarget(packageName: String, activityName: String) {
		try {
			startActivity(
				Intent().setClassName(packageName, activityName)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			)
		} catch (_: ActivityNotFoundException) {
		} catch (_: SecurityException) {
		}
		finish()
	}

	private fun labelFor(packageName: String): String = try {
		packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
	} catch (_: Exception) {
		packageName
	}

	companion object {
		private const val EXTRA_PACKAGE = "package"
		private const val EXTRA_ACTIVITY = "activity"

		fun intent(context: Context, packageName: String, activityName: String): Intent =
			Intent(context, AppLockGateActivity::class.java)
				.putExtra(EXTRA_PACKAGE, packageName)
				.putExtra(EXTRA_ACTIVITY, activityName)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	}
}