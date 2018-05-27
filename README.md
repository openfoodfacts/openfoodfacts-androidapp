Open Food Facts Android Application
===================================



[![Project Status](http://opensource.box.com/badges/active.svg)](http://opensource.box.com/badges)
[![Build Status](https://travis-ci.org/openfoodfacts/OpenFoodFacts-androidApp.svg?branch=master)](https://travis-ci.org/openfoodfacts/OpenFoodFacts-androidApp) 
[![Stories in Ready](https://badge.waffle.io/openfoodfacts/OpenFoodFacts-androidApp.svg?label=ready&title=Ready)](http://waffle.io/openfoodfacts/OpenFoodFacts-androidApp)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/openfoodfacts/OpenFoodFacts-androidApp.svg)](http://isitmaintained.com/project/openfoodfacts/OpenFoodFacts-androidApp "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/openfoodfacts/OpenFoodFacts-androidApp.svg)](http://isitmaintained.com/project/openfoodfacts/OpenFoodFacts-androidApp "Percentage of issues still open")
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/openfoodfacts/localized.svg)](https://crowdin.com/project/openfoodfacts)
<br>
<img src="https://static.openfoodfacts.org/images/misc/openfoodfacts-logo-en-178x150.png">
## What is Open Food Facts?


<a href="https://f-droid.org/repository/browse/?fdid=openfoodfacts.github.scrachx.openfood">
<img src=http://lingoworld.eu/at//public/images/fdroid.png></a>
<a href="https://play.google.com/store/apps/details?id=openfoodfacts.github.scrachx.openfood"><img src=https://play.google.com/intl/en_us/badges/images/badge_new.png></a><br>
(These are beta versions, <a href="https://play.google.com/store/apps/details?id=org.openfoodfacts.scanner">this is the stable version</a>) 

### A food products database

Open Food Facts is a database of food products with ingredients, allergens, nutrition facts and all the tidbits of information we can find on product labels. 

### Made by everyone

Open Food Facts is a non-profit association of volunteers.
1800+ contributors like you have added 43 000+ products from 150 countries using our Android, iPhone or Windows Phone app or their camera to scan barcodes and upload pictures of products and their labels.

### For everyone

Data about food is of public interest and has to be open. The complete database is published as open data and can be reused by anyone and for any use. Check-out the cool reuses or make your own!
- <https://world.openfoodfacts.org>

## Translations

### Initial setup (done)
Translations are made using GetText. We convert translations to and from the Android format using Android2Po.<br><br>
```easy_install android2po```<br>
```a2po COMMAND --android myproject/res --gettext myproject/locale```<br>
```a2po init de fr```<br><br>

### Refreshing exported GetText files
```a2po export```

### Syncing with Launchpad
Ask @teolemon to do it

### Importing back to Android
```a2po import```

### Translate Open Food Facts in your language

You can help translate Open Food Facts and the app at (no technical knowledge required, takes a minute to signup): <br>
https://crowdin.com/project/openfoodfacts

Watch the [topic](https://github.com/openfoodfacts/OpenFoodFacts-androidApp/issues/49).

## Bugs and feature requests

Have a bug or a feature request? Please search for existing and closed issues. If your problem or idea is not addressed yet, please open a new issue.

## Installation

Android Studio packages must be updated by just clicking on install packages in package manager, to let the VCS cloning from the app or the forked app on github then to have it installed and in work on the virtual android device.

## Waffle Throughput Graph

[![Throughput Graph](https://graphs.waffle.io/openfoodfacts/OpenFoodFacts-androidApp/throughput.svg)](https://waffle.io/openfoodfacts/OpenFoodFacts-androidApp/metrics/throughput)

## Libraries used

- http://loopj.com/android-async-http/
- http://jackson.codehaus.org
- https://github.com/code-mc/loadtoast
- https://github.com/dm77/barcodescanner
- https://github.com/koush/ion
- http://jsoup.org/
- https://github.com/satyan/sugar
- https://github.com/afollestad/material-dialogs
- https://github.com/jjhesk/LoyalNativeSlider
- https://github.com/mikepenz/MaterialDrawer
- and others (see gradle)

Big thanks to them!

## Contributing

The project is initially started by [Scot Scriven](https://github.com/itchix), other contributors include:
- [Aurélien Leboulanger](https://github.com/herau)
- [Pierre Slamich](https://github.com/teolemon)
- [Friedger Müffke](https://github.com/friedger)
- [Qian Jin](https://github.com/jinqian)

Feel free to fork the project and send us a pull request.

Here's a few list of bugs:
- Very high impact: https://github.com/openfoodfacts/OpenFoodFacts-androidApp/labels/very%20high%20impact
- https://github.com/openfoodfacts/OpenFoodFacts-androidApp/labels/priority

## Copyright and License

    Copyright 2016 Open Food Facts

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
