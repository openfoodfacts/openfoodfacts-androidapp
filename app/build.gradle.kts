buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")
    }
}
plugins {
    id("com.android.application")
    id("de.timfreiheit.resourceplaceholders.plugin")
    id("org.greenrobot.greendao")
    id("io.sentry.android.gradle")
}

fun obtainTestBuildType(): String {
    // To activate  screenshots buildType in IDE; uncomment next line and comment other
    // otherwise the folder androidTestScreenshots is not recognized as a test folder.
    // val result = "screenshots"
    val result = "debug"

    return project.properties.getOrDefault("testBuildType", result) as String
}

dependencies {
    //Android
    implementation("androidx.browser:browser:1.2.0")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.work:work-runtime:2.3.4")
    implementation("androidx.concurrent:concurrent-futures:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.1.0")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.fragment:fragment:1.2.5")
    implementation("androidx.activity:activity:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.preference:preference:1.1.1")

    //DI
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.work:work-rxjava2:2.3.4")
    annotationProcessor("com.google.dagger:dagger-compiler:2.28")
    implementation("com.google.dagger:dagger:2.28")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    //Rx
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    //Networking
    implementation("com.squareup.retrofit2:retrofit:2.6.4")
    implementation("com.squareup.retrofit2:converter-jackson:2.6.4")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.4")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.11")

    //scheduling jobs
    implementation("commons-lang:commons-lang:2.6")
    implementation("org.apache.commons:commons-csv:1.4")

    //Serialization/Deserialization
    implementation("com.fasterxml.jackson.core:jackson-core:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.11.0")

    //Database
    implementation("org.greenrobot:greendao:3.3.0")

    //Event bus
    implementation("org.greenrobot:eventbus:3.2.0")

    //Image Loading
    implementation("com.squareup.picasso:picasso:2.71828")

    //Image from gallery or camera
    implementation("com.github.jkwiecien:EasyImage:1.4.0")

    //Barcode and QR Scanner
    implementation("com.journeyapps:zxing-android-embedded:3.6.0") {
        isTransitive = false
    }
    implementation("com.google.zxing:core:3.3.0")

    // Apache
    implementation("commons-validator:commons-validator:1.6")

    // UI Component : Custom Toast
    implementation("net.steamcrafted:load-toast:1.0.12")

    // UI Component : ImageView with Zooming
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // UI Component : Material Dialog
    implementation("com.afollestad.material-dialogs:core:0.9.6.0") {
        isTransitive = true
    }

    // UI Component : Material Drawer
    implementation("com.mikepenz:materialdrawer:7.0.0") {
        isTransitive = true
    }

    // UI Component : Adapters

    //DO NOT UPDATE : RecyclerViewCacheUtil removed, needs rework
    implementation("com.mikepenz:fastadapter-commons:3.3.1@aar")
    implementation("com.squareup.retrofit2:converter-scalars:2.1.0")

    // UI Component : Font Icons
    implementation("com.mikepenz:iconics-core:4.0.2@aar")
    implementation("com.mikepenz:google-material-typeface:3.0.1.4.original-kotlin@aar")
    implementation("com.theartofdev.edmodo:android-image-cropper:2.8.0") {
        exclude(group = "com.android.support", module = "appcompat-v7")
    }

    // UI Component : Chips Input
    implementation("com.hootsuite.android:nachos:1.2.0")

    // Crash analytics
    implementation("io.sentry:sentry-android:2.1.6")

    // Unit Testing
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("net.javacrumbs.json-unit:json-unit-fluent:2.17.0")

    // Instrumented tests
    androidTestUtil("androidx.test:orchestrator:1.2.0")

    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.1")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-web:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.2.0") {
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
    compileSdkVersion(29)

    testBuildType = obtainTestBuildType()


    buildFeatures {
        dataBinding = true
    }

    flavorDimensions("versionCode")


    defaultConfig {
        applicationId = "openfoodfacts.github.scrachx.openfood"

        minSdkVersion(16)
        targetSdkVersion(29)

        versionCode = 340
        versionName = "3.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        ndk?.abiFilters("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

        multiDexEnabled = true
        // jackOptions.enabled = true
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
        }

        create("screenshots") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".screenshots"
        }
    }

    productFlavors {
        create("off") {
            applicationId = "openfoodfacts.github.scrachx.openfood"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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


greendao {
    schemaVersion(17)
}



