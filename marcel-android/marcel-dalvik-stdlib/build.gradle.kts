plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
}

val marcelVersion: String = project.findProperty("marcel.version") as String
val javaVersion: String = project.findProperty("java.version") as String

android {
  namespace = "marcel.lang"
  compileSdk = 35

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
    // important to keep metadata (annotations and names of method parameters)
    // be careful, it only works for kotlin source
    javaParameters = true
  }
}

dependencies {
  // dalvik
  implementation(fileTree(Pair("dir", File(rootDir, "app/libs")), Pair("include", listOf("*.jar"))))
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("org.commonmark:commonmark:0.22.0")
  implementation("org.commonmark:commonmark-ext-gfm-tables:0.22.0")

  // marcel libs
  implementation("com.tambapps.marcel:marcel-stdlib:$marcelVersion")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}