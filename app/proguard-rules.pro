# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/var/lib/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class com.bluelinelabs.logansquare.** { *; }
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *
-keep class **$$JsonObjectMapper { *; }
-keep class openfoodfacts.github.scrachx.openfood.models.** { *; }
-keep class openfoodfacts.github.scrachx.openfood.network.deserializers.** { *; }

#Keep Jackson classes ( https://sourceforge.net/p/proguard/discussion/182456/thread/e4d73acf/ )
-keepnames class org.codehaus.jackson.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames interface com.fasterxml.jackson.** { *; }

#Keep Android Support Library
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-ignorewarnings
