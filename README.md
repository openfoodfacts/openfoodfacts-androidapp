<img height='175' src="https://static.openfoodfacts.org/images/misc/openfoodfacts-logo-en-178x150.png" align="left" hspace="1" vspace="1">

# Open Food Facts - Android application

### A food products database

Open Food Facts is a food products database made by everyone, for everyone. Open Food Facts gathers information and data on food products from around the world.


[![Project Status](https://opensource.box.com/badges/active.svg)](https://opensource.box.com/badges)
[![Build Status](https://travis-ci.org/openfoodfacts/openfoodfacts-androidapp.svg?branch=master)](https://travis-ci.org/openfoodfacts/openfoodfacts-androidapp)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=openfoodfacts_openfoodfacts-androidapp&metric=alert_status)](https://sonarcloud.io/dashboard/index/openfoodfacts_openfoodfacts-androidapp)
[![Average time to resolve an issue](https://isitmaintained.com/badge/resolution/openfoodfacts/openfoodfacts-androidapp.svg)](https://isitmaintained.com/project/openfoodfacts/openfoodfacts-androidapp "Average time to resolve an issue")
[![Percentage of issues still open](https://isitmaintained.com/badge/open/openfoodfacts/openfoodfacts-androidapp.svg)](https://isitmaintained.com/project/openfoodfacts/openfoodfacts-androidapp "Percentage of issues still open")
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/openfoodfacts/localized.svg)](https://crowdin.com/project/openfoodfacts)
[![Open Source Helpers](https://www.codetriage.com/openfoodfacts/openfoodfacts-androidapp/badges/users.svg)](https://www.codetriage.com/openfoodfacts/openfoodfacts-androidapp)
<br>

## What is Open Food Facts?

### A food products database

Open Food Facts is a database of food products with ingredients, allergens, nutrition facts and all the tidbits of information we can find on product labels.


### Made by everyone

Open Food Facts is a non-profit association of volunteers.
25000+ contributors like you have added 868000+ products from 150 countries using our Android, iPhone or Windows Phone app or their camera to scan barcodes and upload pictures of products and their labels.

### For everyone

Data about food is of public interest and has to be open. The complete database is published as open data and can be reused by anyone and for any use. Check-out the cool reuses or make your own!
- <https://world.openfoodfacts.org>


### Open Food Facts on [Google Play](https://play.google.com)
------------------

<a href="https://play.google.com/store/apps/details?id=org.openfoodfacts.scanner" alt="Get it on Google Play" target="_blank"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80"></a>

### Open Food Facts on [F-Droid](https://f-droid.org/)
------------------

<a href="https://f-droid.org/packages/openfoodfacts.github.scrachx.openfood/" alt="Get it on F-Droid" target="_blank"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="80"></a>


## Screenshots

<p>
  <img src="https://lh3.googleusercontent.com/VBZ5CfBYqVLpcdRF8TqZmcZaPWo4Ieghp6LmTJ51nU0FbobPXr_C-w3wmWJiYNxn6A=w1260-h646" width="212" height="400" />
  <img src="https://lh3.googleusercontent.com/VB-BSrbx2he7s7O1tGVfCLZNCGiWLy5SvuVpkrAP-Ay9PlLHPnXznyZr2w-0M8XzsA=w1260-h646" width="212" height="400" /> 
  <img src="https://lh3.googleusercontent.com/6Xcs8mxG3x724y5vcTo-M2ujC0QoktyiyUNNd5OzUwruLTZroYXnedY36Q1JgvdK5lA=w1260-h646" width="212" height="400" />
    <img src="https://lh3.googleusercontent.com/627r30FFxVbodLMApJavAhwLym1kSOxSJScXy7DacfAiBPNxjkC6Rsd8Lhjj7yEb_A=w1260-h646" width="212" height="400" />
</p>

## Translations

### Translate Open Food Facts in your language

You can help translate Open Food Facts and the app at (no technical knowledge required, takes a minute to signup): <br>
https://translate.openfoodfacts.org

## Screenshot generation
To generate basic screenshots of the app on your local computer, launch the command `gradlew connectedOffScreenshotsAndroidTest --stacktrace --info -PtestBuildType=screenshots`

## Bugs and feature requests

Have a bug or a feature request? Please search for existing and closed issues. If your problem or idea is not addressed yet, please open a new issue.

## Installation

* [Android Studio](https://developer.android.com/studio/index.html) should be the latest stable build.
* If you're running the app for the first time, Android Studio will ask you to install the Gradle dependencies.
* If you're a new contributor to open-source, we recommend you read our [Setup Guidelines](https://github.com/openfoodfacts/openfoodfacts-androidapp/blob/master/SETUP_GUIDELINES.md)

* Select 'OFF' as the flavor (OBF is Open Beauty Facts, OPF: Open Products Facts, OPFF: Open Pet Food Facts)
<img src="https://user-images.githubusercontent.com/1689815/39445509-8064b2f8-4cbb-11e8-908d-86bcd61cb4f5.png" height="300">

## Libraries Used
We use the following libraries, and we're not closed to changes where relevant :-) 

- [Dagger 2](https://github.com/google/dagger) - A fast dependency injector for Android and Java
- [Retrofit](http://square.github.io/retrofit/) - Retrofit turns your REST API into a Java interface
- [OkHttp](https://github.com/square/okhttp) - An HTTP+SPDY client for Android and Java applications
- [Butterknife](http://jakewharton.github.io/butterknife/) - View "injection" library for Android
- [Mockito](https://github.com/mockito/mockito) - Most popular Mocking framework for unit tests written in Java 
- [Apache](https://github.com/apache/commons-io) - The Apache Commons IO library contains utility classes, stream implementations, file filters, file comparators, endian transformation classes, and much more.
- [RxJava](https://github.com/ReactiveX/RxJava) - Reactive Extensions for the JVM – a library for composing asynchronous and event-based programs using observable sequences for the Java VM.
- [android-async-http](https://loopj.com/android-async-http/) - A Callback-Based Http Client Library for Android
- [Jackson](http://jackson.codehaus.org) - Core part of Jackson that defines Streaming API as well as basic shared abstractions
- [code-mc/loadtoast](https://github.com/code-mc/loadtoast) - Pretty material design toasts with feedback animations
- [dm77/barcodescanner](https://github.com/dm77/barcodescanner) - Barcode Scanner Libraries for Android
- [koush/ion](https://github.com/koush/ion) - Android Asynchronous Networking and Image Loading
- [jsoup](https://jsoup.org/) - jsoup is a Java library for working with real-world HTML
- [satyan/sugar](https://github.com/satyan/sugar) - Insanely easy way to work with Android Database. 
- [afollestad/material-dialogs](https://github.com/afollestad/material-dialogs) - A beautiful, fluid, and extensible dialogs API for Kotlin & Android.
- [jjhesk/LoyalNativeSlider](https://github.com/jjhesk/LoyalNativeSlider) - Possible a dynamic viewpager ever you can find.
- [mikepenz/MaterialDrawer](https://github.com/mikepenz/MaterialDrawer) - The flexible, easy to use, all in one drawer library for your Android project.

Big thanks to their contributors!

## Contributing

The project was initially started by [Scot Scriven](https://github.com/itchix), other contributors include:
- [Aurélien Leboulanger](https://github.com/herau)
- [Pierre Slamich](https://github.com/teolemon)
- [Friedger Müffke](https://github.com/friedger)
- [Qian Jin](https://github.com/jinqian)
- [Fred Deniger](https://github.com/deniger)
- [Full list of the Open Food Facts Android developers](https://github.com/openfoodfacts/openfoodfacts-androidapp/graphs/contributors)

### Who do I talk to?

* Repo owner or admin
* contact@openfoodfacts.org
* Join our discussion room at <https://slack.openfoodfacts.org/>


Will you join us ?

If you're new to open-source, we recommend to checkout our [_Contributing Guidelines_](https://github.com/openfoodfacts/openfoodfacts-androidapp/blob/master/CONTRIBUTING.md). Feel free to fork the project and send a pull request.

Here's a few list of bugs:
- Very high impact: https://github.com/openfoodfacts/openfoodfacts-androidapp/labels/very%20high%20impact
- Priority: https://github.com/openfoodfacts/openfoodfacts-androidapp/labels/priority

If you don't have time to contribute code, you're very welcome to scan new products, or make a donation to [_help pay for the hosting and general costs_](https://donate.openfoodfacts.org) 

## Copyright and License

    Copyright 2016-2019 Open Food Facts

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and 
    limitations under the License.
