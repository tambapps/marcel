import java.util.Properties

plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
}

val marcelProperties = Properties().apply { File(rootDir, "marcel.properties").inputStream().use(this::load) }
val marcelVersion: String = marcelProperties.getProperty("marcel.version")

android {
  namespace = "com.tambapps.marcel.android.compiler"
  compileSdk = 34

  defaultConfig {
    minSdk = 26

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")

    // used to be able to access min sdk version from app
    buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
  }

  buildFeatures {
    buildConfig = true
  }
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

dependencies {
  // dalvik
  implementation(fileTree(Pair("dir", File(rootDir, "app/libs")), Pair("include", listOf("*.jar"))))


  // marcel
  implementation("com.tambapps.marcel:marcel-repl:$marcelVersion")
  implementation(project(path = ":marcel-dalvik-stdlib"))

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}