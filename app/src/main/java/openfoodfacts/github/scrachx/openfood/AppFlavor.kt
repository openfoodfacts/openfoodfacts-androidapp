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
package openfoodfacts.github.scrachx.openfood


enum class AppFlavor(val versionCode: String) {
    OFF("off"),
    OPFF("opff"),
    OPF("opf"),
    OBF("obf");

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun isFlavors(vararg flavors: AppFlavor) = currentFlavor in flavors

        val currentFlavor = values().first { BuildConfig.FLAVOR_versionCode in it.versionCode }
    }
}