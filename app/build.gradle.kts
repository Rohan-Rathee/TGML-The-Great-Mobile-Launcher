plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "com.example.tgml_the_great_mobile_launcher"
	compileSdk = 37 // Direct assignment is standard in modern Gradle

	// 1. ADD SIGNING CONFIGURATION
	signingConfigs {
		create("release") {
			storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
			storePassword = "android"
			keyAlias = "androiddebugkey"
			keyPassword = "android"
		}
	}

	defaultConfig {
		applicationId = "com.example.tgml_the_great_mobile_launcher"
		minSdk = 24
		targetSdk = 37
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			// 2. ASSIGN THE SIGNING CONFIG TO THE RELEASE BUILD
			signingConfig = signingConfigs.getByName("release")

			optimization {
				enable = true
				packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
			}
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	buildFeatures {
		compose = true
	}
}


dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui.graphics)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}