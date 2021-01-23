/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven("https://jitpack.io")
    }
}
plugins {
    id("com.android.application")
    id("de.timfreiheit.resourceplaceholders.plugin")
    id("org.greenrobot.greendao")
    id("io.sentry.android.gradle")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

fun obtainTestBuildType(): String {
    // To activate  screenshots buildType in IDE; uncomment next line and comment other
    // otherwise the folder androidTestScreenshots is not recognized as a test folder.
    //val result = "screenshots"
    val result = "debug"

    return project.properties.getOrDefault("testBuildType", result) as String
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")

    // Android KTX
    implementation("androidx.fragment:fragment-ktx:1.3.0-rc01")
    implementation("androidx.activity:activity-ktx:1.2.0-rc01")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.core:core-ktx:1.5.0-alpha05")


    // AndroidX
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.browser:browser:1.3.0")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.work:work-runtime:2.4.0")
    implementation("androidx.work:work-rxjava2:2.4.0")
    implementation("androidx.startup:startup-runtime:1.0.0")


    kapt("com.google.dagger:dagger-compiler:2.30.1")
    implementation("com.google.dagger:dagger:2.30.1")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    //Rx
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("io.reactivex.rxjava2:rxjava:2.2.20")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    //Networking
    implementation("com.squareup.retrofit2:retrofit:2.6.4")
    implementation("com.squareup.retrofit2:converter-jackson:2.6.4")
    implementation("com.squareup.retrofit2:converter-scalars:2.1.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.4")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.11")


    // Apache commons
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("commons-validator:commons-validator:1.7")

    // Serialization/Deserialization
    implementation("com.fasterxml.jackson.core:jackson-core:${rootProject.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${rootProject.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${rootProject.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${rootProject.extra["jacksonVersion"]}")


    // Database
    implementation("org.greenrobot:greendao:${rootProject.extra["greendaoVersion"]}")

    // Event bus and index
    val eventBusVersion = "3.2.0"
    implementation("org.greenrobot:eventbus:$eventBusVersion")
    kapt("org.greenrobot:eventbus-annotation-processor:$eventBusVersion")

    implementation("com.google.android.material:material:1.2.1")

    //Image Loading
    implementation("com.squareup.picasso:picasso:2.71828")

    //Image from gallery or camera
    implementation("com.github.jkwiecien:EasyImage:1.4.0")

    //Barcode and QR Scanner
    implementation("com.google.zxing:core:3.3.0")
    implementation("com.journeyapps:zxing-android-embedded:3.6.0") { isTransitive = false }

    // UI Component : Custom Toast
    implementation("net.steamcrafted:load-toast:1.0.12")

    // UI Component : ImageView with Zooming
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // UI Component : Material Dialog
    implementation("com.afollestad.material-dialogs:core:0.9.6.0") {
        isTransitive = true
    }

    // UI Component : Material Drawer
    implementation("com.mikepenz:materialdrawer:7.0.0") { isTransitive = true }

    //DO NOT UPDATE : RecyclerViewCacheUtil removed, needs rework
    implementation("com.mikepenz:fastadapter-commons:3.3.1@aar")

    // UI Component : Font Icons
    implementation("com.mikepenz:iconics-core:4.0.2@aar")
    implementation("com.mikepenz:google-material-typeface:3.0.1.6.original-kotlin@aar")
    implementation("com.theartofdev.edmodo:android-image-cropper:2.8.0") {
        exclude("com.android.support", "appcompat-v7")
    }

    // UI Component : Chips Input
    implementation("com.hootsuite.android:nachos:1.2.0")

    // Crash analytics
    implementation("io.sentry:sentry-android:3.2.0")

    // Unit Testing
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.mockito:mockito-core:3.6.28")
    testImplementation("net.javacrumbs.json-unit:json-unit-fluent:2.22.0")
    testImplementation("com.google.truth:truth:1.1")
    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1")

    // Instrumented tests
    androidTestUtil("androidx.test:orchestrator:1.3.0")

    androidTestImplementation("androidx.test:runner:1.3.0") {
        exclude("junit")
    }
    androidTestImplementation("androidx.test:rules:1.3.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.2") {
        exclude("junit")
    }

    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-web:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0") {
        exclude(group = "com.android.support", module = "appcompat-v7")
        exclude(group = "com.android.support", module = "support-v4")
        exclude(group = "com.android.support", module = "design")
        exclude(module = "recyclerview-v7")
    }
    androidTestImplementation("com.jraska:falcon:2.1.1")
    androidTestImplementation("tools.fastlane:screengrab:1.2.0")

    resourcePlaceholders {
        files = listOf("xml/shortcuts.xml")
    }

    // ShowCaseView dependency
    implementation("com.github.mreram:showcaseview:1.0.5")
}


android {
    compileSdkVersion(30)

    testBuildType = obtainTestBuildType()


    buildFeatures {
        dataBinding = true
    }

    flavorDimensions("versionCode")


    defaultConfig {
        applicationId = "openfoodfacts.github.scrachx.openfood"

        minSdkVersion(16)
        targetSdkVersion(30)

        versionCode = 431
        versionName = "3.6.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        ndk.abiFilters("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

        multiDexEnabled = true
        // jackOptions.enabled = true
    }

    signingConfigs {
        create("release") {
            if (System.getenv("CI_RELEASE")?.toBoolean() == true) { // CI=true is exported by github action
                val storeFilePath = System.getenv("SIGN_STORE_PATH")
                if (storeFilePath != null) {
                    storeFile = file(storeFilePath)
                }
                storePassword = System.getenv("SIGN_STORE_PASSWORD")
                keyAlias = System.getenv("SIGN_KEY_ALIAS")
                keyPassword = System.getenv("SIGN_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isCrunchPngs = true

            // Enables code shrinking, obfuscation, and optimization for only
            // release build type.
            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }

        create("screenshots") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".screenshots"
        }
    }

    productFlavors {
        create("off") {
            applicationId = "openfoodfacts.github.scrachx.openfood"
            if ("true" == System.getenv("CI_RELEASE")) { // CI=true is exported by github action
                applicationId = "org.openfoodfacts.scanner"
            }
            resValue("string", "app_name", "OpenFoodFacts")
            buildConfigField("String", "APP_NAME", "\"Open Food Facts\"")
            buildConfigField("String", "HOST", "\"https://ssl-api.openfoodfacts.org\"")
            buildConfigField("String", "OFOTHERLINKAPP", "\"org.openbeautyfacts.scanner\"")
            buildConfigField("String", "OFWEBSITE", "\"https://world.openfoodfacts.org/\"")
            buildConfigField("String", "WIKIDATA", "\"https://www.wikidata.org/wiki/Special:EntityData/\"")
            buildConfigField("String", "STATICURL", "\"https://static.openfoodfacts.org\"")
        }
        create("obf") {
            applicationId = "openfoodfacts.github.scrachx.openbeauty"
            resValue("string", "app_name", "OpenBeautyFacts")
            buildConfigField("String", "APP_NAME", "\"Open Beauty Facts\"")
            buildConfigField("String", "HOST", "\"https://ssl-api.openbeautyfacts.org\"")
            buildConfigField("String", "OFOTHERLINKAPP", "\"org.openfoodfacts.scanner\"")
            buildConfigField("String", "OFWEBSITE", "\"https://world.openbeautyfacts.org/\"")
            buildConfigField("String", "WIKIDATA", "\"https://www.wikidata.org/wiki/Special:EntityData/\"")
            buildConfigField("String", "STATICURL", "\"https://static.openbeautyfacts.org\"")
        }
        create("opff") {
            applicationId = "org.openpetfoodfacts.scanner"
            resValue("string", "app_name", "OpenPetFoodFacts")
            buildConfigField("String", "APP_NAME", "\"Open Pet Food Facts\"")
            buildConfigField("String", "HOST", "\"https://ssl-api.openpetfoodfacts.org\"")
            buildConfigField("String", "OFOTHERLINKAPP", "\"org.openfoodfacts.scanner\"")
            buildConfigField("String", "OFWEBSITE", "\"https://world.openpetfoodfacts.org/\"")
            buildConfigField("String", "WIKIDATA", "\"https://www.wikidata.org/wiki/Special:EntityData/\"")
            buildConfigField("String", "STATICURL", "\"https://static.openpetfoodfacts.org\"")
        }
        create("opf") {
            applicationId = "org.openproductsfacts.scanner"
            resValue("string", "app_name", "OpenProductsFacts")
            buildConfigField("String", "APP_NAME", "\"Open Products Facts\"")
            buildConfigField("String", "HOST", "\"https://ssl-api.openproductsfacts.org\"")
            buildConfigField("String", "OFOTHERLINKAPP", "\"org.openfoodfacts.scanner\"")
            buildConfigField("String", "OFWEBSITE", "\"https://world.openproductsfacts.org/\"")
            buildConfigField("String", "WIKIDATA", "\"https://www.wikidata.org/wiki/Special:EntityData/\"")
            buildConfigField("String", "STATICURL", "\"https://static.openproductsfacts.org\"")
        }
    }


    dexOptions {
        preDexLibraries = false
        javaMaxHeapSize = "4g"
    }

    compileOptions {
        // Sets Java compatibility to Java 8
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    lintOptions {
        isAbortOnError = false
        disable("MissingTranslation")
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES.txt")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/LICENSE")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/notice.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/dependencies.txt")
        exclude("META-INF/LGPL2.1")
    }

    testOptions {
        // avoid "Method ... not mocked."
        unitTests.isReturnDefaultValues = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

    }
}

kapt {
    arguments {
        arg("eventBusIndex", "openfoodfacts.github.scrachx.openfood.app.OFFEventsIndex")
    }
}

greendao { schemaVersion(20) }






