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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath(libs.plugin.gradle.android)
        classpath(libs.plugin.gradle.greendao)
        classpath(libs.plugin.gradle.hilt)
        classpath(libs.plugin.gradle.kotlin)
    }
}

plugins {
    alias(libs.plugins.resourceplaceholders) apply false // Android Extension in `app` breaks without this
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.updates)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    sonarqube {
        properties {
            property("sonar.exclusions", "**/openfoodfacts/github/scrachx/openfood/models/*,**/*.xml")
            property("sonar.coverage.exclusions", "**/openfoodfacts/github/scrachx/openfood/models/*")
        }
    }
}

detekt {
    source = files("./app/src/")
    ignoreFailures = true
}

// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask>().configureEach {
    rejectVersionIf {
        val components = candidate.version.split('-')
        if (components.size <= 1) return@rejectVersionIf false
        val build = components[1]
        val preReleaseBuildPrefixes = listOf("alpha", "beta", "rc", "eap")
        preReleaseBuildPrefixes.any { preReleaseBuildPrefix ->
            build.startsWith(prefix = preReleaseBuildPrefix, ignoreCase = true)
        }
    }
}

// https://github.com/littlerobots/version-catalog-update-plugin
versionCatalogUpdate {
    sortByKey.set(false)
}
