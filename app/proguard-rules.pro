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

<<<<<<< HEAD
-keep class **$$JsonObjectMapper { *; }
-keep class openfoodfacts.github.scrachx.openfood.network.services.* { *; }

# Keep Jackson classes ( https://sourceforge.net/p/proguard/discussion/182456/thread/e4d73acf/ )
=======
-keep class com.bluelinelabs.logansquare.** { *; }
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *
-keep class **$$JsonObjectMapper { *; }
-keep class openfoodfacts.github.scrachx.openfood.models.** { *; }
-keep class openfoodfacts.github.scrachx.openfood.network.deserializers.** { *; }

#Keep Jackson classes ( https://sourceforge.net/p/proguard/discussion/182456/thread/e4d73acf/ )
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
-keepnames class org.codehaus.jackson.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames interface com.fasterxml.jackson.** { *; }

<<<<<<< HEAD
# To display labels on the BottomNavigationView
-keep class com.google.android.material.bottomnavigation.BottomNavigationMenuView

# For stack-traces
-keepattributes SourceFile,LineNumberTable
-ignorewarnings

# EventBus
=======
#Keep Android Support Library
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
# to display labels on the BottomNavigationView
-keepclassmembers class android.support.design.internal.BottomNavigationMenuView {
    boolean mShiftingMode;
}
# for stack-traces
-keepattributes SourceFile,LineNumberTable
-ignorewarnings

# for event bus
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
<<<<<<< HEAD
}

# GreenDAO generated classes
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties { *; }
# GreenDAO entities
-keep class openfoodfacts.github.scrachx.openfood.models.** { *; }
=======
}
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
