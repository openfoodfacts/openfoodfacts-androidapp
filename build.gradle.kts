// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath("org.greenrobot:greendao-gradle-plugin:3.3.0")
        classpath("com.github.timfreiheit:ResourcePlaceholdersPlugin:0.2")

        classpath("io.sentry:sentry-android-gradle-plugin:1.7.35")
    }
}

plugins {
    id("org.sonarqube") version "3.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")
    }

    sonarqube { properties { property("sonar.exclusions", "**/openfoodfacts/github/scrachx/openfood/models/*,**/*.xml") } }

    sonarqube { properties { property("sonar.coverage.exclusions", "**/openfoodfacts/github/scrachx/openfood/models/*") } }

}
