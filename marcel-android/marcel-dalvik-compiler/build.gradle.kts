plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
}

val marcelVersion: String = project.findProperty("marcel.version") as String
val javaVersion: String = project.findProperty("java.version") as String

android {
  namespace = "com.tambapps.marcel.android.compiler"
  compileSdk = 35

  defaultConfig {
    minSdk = 30

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")

    // used to be able to access min sdk version from app
    buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
    buildConfigField("String", "JAVA_VERSION", "\"$javaVersion\"")
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


  // marcel
  implementation("com.tambapps.marcel:marcel-repl:$marcelVersion")
  implementation(project(path = ":marcel-dalvik-stdlib"))

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}