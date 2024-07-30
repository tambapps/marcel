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
    javaParameters = true // important to keep metadata (annotations and names of method parameters)
  }
}

dependencies {
  // dalvik
  implementation(fileTree(Pair("dir", File(rootDir, "app/libs")), Pair("include", listOf("*.jar"))))
  implementation("androidx.core:core-ktx:1.13.1")

  // marcel libs
  implementation("com.tambapps.marcel:marcel-stdlib:$marcelVersion")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}