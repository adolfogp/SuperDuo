# Super Duo!

Project developed as an assignment for [Udacity's Android Developer Nanodegree]
(https://www.udacity.com), based on source code provided by Udacity. A list of
features that may be added in future versions can be found at [TODO.md](./TODO.md).

The project contains two applications:

* Alexandria
* Football Scores

## Building the applications

### Requirements:

* [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads).
  This project uses Java 8's lambda syntax, so a lower version may not be used.
* [Android SDK](https://developer.android.com/sdk/installing/index.html)

### Compiling and installing

**Important:** If you're using [Android Studio]
(http://developer.android.com/tools/studio/index.html), make sure you have the
latest version, and if the project shows error when you open it, clean the
project and if needed, rebuild it. That is, go to _Build > Clean Project_, and
if errors are still reported execute _Build > Rebuild Project_.

This project uses [Gradle](https://gradle.org/) and the different build tasks may
be executed using the provided Gradle Wrapper scripts. For example, to list the
available tasks you may execute:

    $ ./gradlew tasks

You may create the [APK](https://developer.android.com/tools/building/index.html)
by invoking:

    $ ./gradlew assembleDebug
or

    $ ./gradlew build

The resulting APK may be found at `app/build/outputs`.

## Frameworks and libraries used

The applications use the following frameworks and libraries:

* [ZXing Android Embedded](http://square.github.io/retrofit/) to scan barcodes.
* [Data Binding Library]
  (https://developer.android.com/tools/data-binding/guide.html) to follow the
  Model-View-ViewModel pattern. More information can be found in the following
  articles:
    - [Android databinding: Goodbye presenter, hello ViewModel!]
      (http://tech.vg.no/2015/07/17/android-databinding-goodbye-presenter-hello-viewmodel/)
    - [Don't forget the View Model!]
      (http://tech.vg.no/2015/04/06/dont-forget-the-view-model/)
* [Retrofit](http://square.github.io/retrofit/) to consume the RESTful API.
* [Picasso](http://square.github.io/picasso/) to download, cache and display images.
* [Dagger 2](http://google.github.io/dagger/) for dependency injection.
* [Retrolambda](https://github.com/orfjackal/retrolambda) in order to use
  Java 8's lambda syntax.
* [EventBus](https://github.com/greenrobot/EventBus) to handle communication
  between Fragments and Activities.
* [Parceler](https://github.com/johncarl81/parceler) to generate Parcelable
  wrappers of domain model classes.
* [Commons Lang](https://commons.apache.org/proper/commons-lang/) for common
  tasks, like implementing `equals`, `hashCode` and `toString`.

## Licencing and attributions

The original source code was provided by [Udacity](https://www.udacity.com),
modifications and additions were made by Jesús Adolfo García Pasquel. For
licencing information, read [LICENSE.txt](./LICENSE.txt) and
 [NOTICE.txt](./NOTICE.txt).

    Copyright 2015 Jesús Adolfo García Pasquel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
