import java.util.Properties

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
  id("com.google.dagger.hilt.android")
}

val marcelProperties = Properties().apply { File(rootDir, "marcel.properties").inputStream().use(this::load) }
val marcelVersion: String = marcelProperties.getProperty("marcel.version")
val javaVersion: String = marcelProperties.getProperty("java.version")

android {
  namespace = "com.tambapps.marcel.android.marshell"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.tambapps.marcel.android.marshell"
    minSdk = 30
    targetSdk = 34
    versionCode = 7
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
  }
  kotlinOptions {
    jvmTarget = javaVersion
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.13"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  // d8 (got from Android SDK, read README for more information)
  implementation(fileTree(Pair("dir", File(rootDir, "app/libs")), Pair("include", listOf("*.jar"))))

  // marcel
  implementation("com.tambapps.marcel:marcel-repl:$marcelVersion")
  implementation("com.tambapps.marcel:dumbbell-core:$marcelVersion")
  implementation(project(path = ":marcel-dalvik-compiler"))
  implementation(project(path = ":marcel-dalvik-stdlib"))

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation("androidx.navigation:navigation-compose:2.7.7")
  implementation(platform("androidx.compose:compose-bom:2024.05.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.datastore:datastore-preferences:1.1.1")
  implementation("androidx.work:work-runtime-ktx:2.9.0")
  implementation("androidx.compose.ui:ui-text-google-fonts:1.6.7")
  implementation("androidx.browser:browser:1.8.0")

  // hilt
  implementation("com.google.dagger:hilt-android:2.51.1")
  ksp("com.google.dagger:hilt-android-compiler:2.51.1")
  ksp("androidx.hilt:hilt-compiler:1.2.0")
  implementation("androidx.hilt:hilt-work:1.2.0")
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

  // room
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  annotationProcessor("androidx.room:room-compiler:2.6.1")
  ksp("androidx.room:room-compiler:2.6.1")

  // tests
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}
