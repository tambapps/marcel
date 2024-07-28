plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
}

val marcelVersion: String = project.findProperty("marcel.version") as String
val javaVersion: String = project.findProperty("java.version") as String

android {
  namespace = "marcel.lang"
  compileSdk = 34

  defaultConfig {
    minSdk = 30

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
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
}

dependencies {
  // dalvik
  implementation(fileTree(Pair("dir", File(rootDir, "app/libs")), Pair("include", listOf("*.jar"))))

  // marcel libs
  implementation("com.tambapps.marcel:marcel-stdlib:$marcelVersion")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}