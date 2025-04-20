# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# VERY IMPORTANT
-keepattributes MethodParameters

# Java stuff
-keep class java.** { *; }
-keep interface java.** { *; }
-keep enum java.** { *; }
-keepclassmembernames class java.** { *; }
-keepclassmembernames interface java.** { *; }
-keepclassmembernames enum java.** { *; }

# Needed in order for xml parsing to work
-keep class javax.xml.** { *; }
-keep class org.w3c.** { *; }
-dontwarn org.w3c.**

# Marcel stuff
-keep class marcel.** { *; }
-keep interface marcel.** { *; }
-keep enum marcel.** { *; }
-keepclassmembernames class marcel.** { *; }
-keepclassmembernames interface marcel.** { *; }
-keepclassmembernames enum marcel.** { *; }

-keep class com.tambapps.** { *; }
-keep interface com.tambapps.** { *; }
-keep enum com.tambapps.** { *; }
-keepclassmembernames class com.tambapps.** { *; }
-keepclassmembernames interface com.tambapps.** { *; }
-keepclassmembernames enum com.tambapps.** { *; }

-keep class org.codehaus.** { *; }

# Android stuff
-keep class android.** { *; }
-keep interface android.** { *; }
-keep enum android.** { *; }
-keepclassmembernames class android.** { *; }
-keepclassmembernames interface android.** { *; }
-keepclassmembernames enum android.** { *; }