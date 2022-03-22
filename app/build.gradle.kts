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
plugins {
    id("com.android.application")
    id("de.timfreiheit.resourceplaceholders.plugin")
    id("org.greenrobot.greendao")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.dokka") version "1.6.10"
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

    // Kotlin coroutines
    val coroutinesVersion = "1.6.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Android KTX
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.core:core-ktx:1.7.0")

    val lifecycleVer = "2.4.0-alpha03"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVer")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVer")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVer")

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.annotation:annotation:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.startup:startup-runtime:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ML Kit barcode Scanner
    implementation("com.google.mlkit:barcode-scanning:17.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:${rootProject.extra["hiltVersion"]}")
    kapt("com.google.dagger:hilt-compiler:${rootProject.extra["hiltVersion"]}")

    // WorkManager with Hilt
    implementation("androidx.hilt:hilt-work:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    // Reactive Streams
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.jakewharton.rxrelay2:rxrelay:2.1.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.6.4")
    implementation("com.squareup.retrofit2:converter-jackson:2.6.4")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.4")
    implementation("com.squareup.retrofit2:converter-scalars:2.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.13")

    // Logging
    implementation("com.squareup.logcat:logcat:0.1")

    // Apache commons
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("commons-validator:commons-validator:1.7")

    // Serialization/Deserialization
    implementation("com.fasterxml.jackson.core:jackson-core:${rootProject.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${rootProject.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${rootProject.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${rootProject.extra["jacksonVersion"]}")

    // Database
    implementation("org.greenrobot:greendao:${rootProject.extra["greendaoVersion"]}")

    // Event bus and index
    val eventBusVersion = "3.3.1"
    implementation("org.greenrobot:eventbus:$eventBusVersion")
    kapt("org.greenrobot:eventbus-annotation-processor:$eventBusVersion")

    // Material design
    implementation("com.google.android.material:material:1.4.0")

    // Image Loading
    implementation("com.squareup.picasso:picasso:2.8")

    // Image from gallery or camera
    implementation("com.github.jkwiecien:EasyImage:1.4.0")

    // Barcode and QR Scanner
    // TODO: cannot upgrade, requires API 24 or higher
    implementation("com.google.zxing:core:3.3.0")

    implementation("com.journeyapps:zxing-android-embedded:3.6.0") { isTransitive = false }

    // UI Component : ImageView with Zooming
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // UI Component : Material Drawer
    // https://github.com/mikepenz/MaterialDrawer/commit/3b2cb1db4c3b6afe639b0f3c21c03c1de68648a3
    // TODO: We need minSdk 16 to update
    implementation("com.mikepenz:materialdrawer:7.0.0") { isTransitive = true }

    // DO NOT UPDATE : RecyclerViewCacheUtil removed, needs rework
    implementation("com.mikepenz:fastadapter-commons:3.3.1@aar")

    // UI Component : Font Icons
    implementation("com.mikepenz:iconics-core:4.0.2@aar")
    implementation("com.mikepenz:google-material-typeface:3.0.1.6.original-kotlin@aar")
    implementation("com.github.CanHub:Android-Image-Cropper:3.1.3")

    // UI Component : Chips Input
    implementation("com.github.hootsuite:nachos:1.2.0")

    // Crash analytics
    implementation("io.sentry:sentry-android:5.7.0")
    implementation("com.github.matomo-org:matomo-sdk-android:v4.1.2")

    // ShowCaseView dependency
    implementation("com.github.mreram:showcaseview:1.0.5")

    // Unit Testing
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.7.3")
    testImplementation("org.mockito:mockito-core:4.4.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.4.0")
    testImplementation("net.javacrumbs.json-unit:json-unit-fluent:2.28.0")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${coroutinesVersion}")

    val junit5Bom = "5.8.2"
    testImplementation(platform("org.junit:junit-bom:$junit5Bom"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

    // Instrumented tests
    androidTestUtil("androidx.test:orchestrator:1.4.1")

    // Hilt for Android Testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.39.1")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.39.1")
    androidTestImplementation("com.google.dagger:dagger:2.39.1")
    kaptAndroidTest("com.google.dagger:dagger-compiler:2.39.1")

    androidTestImplementation("androidx.test:runner:1.3.0") { exclude("junit") }
    androidTestImplementation("androidx.test:rules:1.4.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.2") { exclude("junit") }

    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-web:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0") {
        exclude(group = "com.android.support", module = "appcompat-v7")
        exclude(group = "com.android.support", module = "support-v4")
        exclude(group = "com.android.support", module = "design")
        exclude(module = "recyclerview-v7")
    }
    androidTestImplementation("com.jraska:falcon:2.2.0")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${coroutinesVersion}")


    resourcePlaceholders { files = listOf("xml/shortcuts.xml") }
}

android {
    compileSdk = 31

    testBuildType = obtainTestBuildType()

    buildFeatures {
        viewBinding = true
    }

    flavorDimensions += listOf("versionCode", "platform")

    defaultConfig {
        applicationId = "openfoodfacts.github.scrachx.openfood"

        minSdk = 16
        targetSdk = 31

        versionCode = 433
        versionName = "3.6.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        ndk.abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

        multiDexEnabled = true
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
        release {
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

        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true

            defaultConfig.minSdk = 18

            // Uncomment to use dev server
//            buildConfigField("String", "HOST", "\"https://ssl-api.openfoodfacts.net\"")
//            buildConfigField("String", "OFWEBSITE", "\"https://www.openfoodfacts.net/\"")
//            buildConfigField("String", "STATICURL", "\"https://static.openfoodfacts.net\"")
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
            buildConfigField("String", "MATOMO_URL", "\"https://analytics.openfoodfacts.org/matomo.php\"")
            dimension = "versionCode"
        }
        create("obf") {
            applicationId = "openfoodfacts.github.scrachx.openbeauty"
            if ("true" == System.getenv("CI_RELEASE")) { // CI=true is exported by github action
                applicationId = "org.openbeautyfacts.scanner"
            }
            resValue("string", "app_name", "OpenBeautyFacts")
            buildConfigField("String", "APP_NAME", "\"Open Beauty Facts\"")
            buildConfigField("String", "HOST", "\"https://ssl-api.openbeautyfacts.org\"")
            buildConfigField("String", "OFOTHERLINKAPP", "\"org.openfoodfacts.scanner\"")
            buildConfigField("String", "OFWEBSITE", "\"https://world.openbeautyfacts.org/\"")
            buildConfigField("String", "WIKIDATA", "\"https://www.wikidata.org/wiki/Special:EntityData/\"")
            buildConfigField("String", "STATICURL", "\"https://static.openbeautyfacts.org\"")
            buildConfigField("String", "MATOMO_URL", "\"https://analytics.openfoodfacts.org/matomo.php\"")
            dimension = "versionCode"
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
            buildConfigField("String", "MATOMO_URL", "\"https://analytics.openfoodfacts.org/matomo.php\"")
            dimension = "versionCode"
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
            buildConfigField("String", "MATOMO_URL", "\"https://analytics.openfoodfacts.org/matomo.php\"")
            dimension = "versionCode"
        }


        create("playstore") {
            dimension = "platform"

            buildConfigField("boolean", "USE_MLKIT", "true")
        }

        create("fdroid") {
            dimension = "platform"

            buildConfigField("boolean", "USE_MLKIT", "false")
        }
    }

    compileOptions {
        // Sets Java compatibility to Java 8
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    lint {
        isAbortOnError = false

        disable(
            "MissingTranslation",
            "ImpliedQuantity",
            // Invalid Resource Folder is for values-b+sr… folders
            "InvalidResourceFolder"
        )
    }

    packagingOptions {
        resources.excludes += listOf(
            "META-INF/DEPENDENCIES.txt",
            "META-INF/LICENSE.txt",
            "META-INF/NOTICE.txt",
            "META-INF/NOTICE",
            "META-INF/LICENSE",
            "META-INF/DEPENDENCIES",
            "META-INF/notice.txt",
            "META-INF/license.txt",
            "META-INF/dependencies.txt",
            "META-INF/LGPL2.1"
        )
    }

    testOptions {
        unitTests.all { test ->
            test.useJUnitPlatform {
                includeEngines("junit-jupiter", "junit-vintage")
            }
            test.testLogging {
                events("passed", "skipped", "failed")
            }
        }

        // avoid "Method ... not mocked."
        unitTests.isReturnDefaultValues = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("eventBusIndex", "openfoodfacts.github.scrachx.openfood.app.OFFEventsIndex")
    }
}

greendao { schemaVersion(22) }
