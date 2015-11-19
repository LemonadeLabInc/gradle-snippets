Lemonade Lab Gradle Plugins
===========================

This project contains a set of [Gradle](http://gradle.org/) plugins to
customize the standard build conventions to the ones used at
[Lemonade Lab](http://lemona.de/)

* [Main Plugin](#main-plugin)
* [Android Plugin](#android-plugin)
* [Publishing Plugin](#publishing-plugin)
* [S3 Repository Plugin](#s3-repository-plugin)


Main Plugin
-----------

```groovy
apply plugin: 'de.lemona.gradle'
```

This is the _master of all evils_ plugin. It will basically configure almost
anything else depending on the other plugins configured in your `build.gradle`
file.

This plugin will:

1. Read the `~/.lemonade.properties` file (if present) and inject all properties
   as _extra project properties_.
   > <small>_Note:_ any property specified on the command line as `-Pname=value`
   > will be left untouched.</small>
2. Read the `lemonade.properties` relative to the current project (and to the
   current project's root project) and inject all properties as _extra project
   properties_.
   > <small>_Note:_ any property specified on the command line as `-Pname=value`
   > or in the user's `~/.lemonade.properties` read by the step above file will
   > be left untouched.</small>
3. Initialize the project's `version` filed following Lemonade's
   [versioning convertions](#versioning)
4. Set up three repositories for dependency resolution:
   * Lemonade's OSS repository on [Bintray](https://bintray.com/lemonade/maven).
   * Bintray's [JCenter](https://bintray.com/lemonade/maven) repository.
   * The [Maven Central](http://search.maven.org/) repository.
5. Set up the rest of our plugins:
   * Add the [Android Plugin](#android-plugin) if either the
     `com.android.application` or `com.android.library` plugins were specified
     in the build file.
   * Add the [Publishing Plugin](#publishing-plugin) if the `maven-publish`
     plugin was specified in the build file.


Android Plugin
--------------

```groovy
apply plugin: 'de.lemona.gradle.android'
```

This simple plugin will only highlight test results in the console output
(for better integration with build systems) and create a `javadoc` task per
each build _variant_ (`debug`, `release`, ...).


Publishing Plugin
-----------------

```groovy
apply plugin: 'de.lemona.gradle.publishing'
```

This plugin will publish Java and Android artifacts into a local Maven
repository under `${buildDir}/maven`.

For Java artifacts the main java component, sources and javadoc jars will
be published.

For Android _libraries_ the the jar, aar, sources and javadoc jars will be
published under the `${buildDir}/maven/${variant.name}` directory.

By default the `release` variant will be published, to publish a _different_
build variant specify its name in the `publishVariant` project property or
`PUBLISH_VARIANT` environment variable.


S3 Repository Plugin
--------------------

```groovy
apply plugin: 'de.lemona.gradle.s3'
```

> _Note:_ This plugin is not automatically configured by the
> [Main Plugin](#main-plugin), but needs to be **manually specified**

This plugin requires three parameters:

* `s3.repository` property or `S3_REPOSITORY` environment variable
  * Specifies the URL of the S3 maven repository
* `s3.accessKey` property or `S3_ACCESS_KEY` environment variable
  * Specifies the access key to be used to access the S3 repository
* `s3.secretKey` property or `S3_SECRET_KEY` environment variable
  * Specifies the secret key to be used to access the S3 repository

This plugin will allow dependencies to be resolved against an Amazon S3
backed Maven repository.

If the `maven-publish` plugin is also present, all publications will *also*
be uploaded to the same repository (using the same credentials).



Versioning
----------

The original `version` must be a `String` in the _`major.minor`_ format.

The [Main Plugin](#main-plugin) will parse and overwrite gradle's `version`
field to be an object containing the following fields:

* `major`: the major version number (from 0 to 127)
* `minor`: the minor version number (from 0 to 255)
* `build`: the build number (from 0 to 65533) or `-1` if this is a snapshot
* `versionCode`: the version code (calculated as a 32-bit integer from `major`,
  `minor` and `build`) for release or `1` for snapshot builds
* `isSnapshot`: a flag indicating a release or snapshot build

The final `toString()` representation of the `version` will be either
_`minor.major.build`_ or _`minor.major-SNAPSHOT`_

The `versionCode` will always be `1` for all snapshot builds. In case of a
release build, it will be a 32-bit signed positive number containing:

|   Bit 0 ... 7   |   Bit 8 ... 15   |  Bit 16 ... 31  |
|:---------------:|:----------------:|:---------------:|
| `major` version | `minor`  version | `build`  plus 2 |

Henceforth, version `0.0.0` will have `versionCode=2` while version `1.2.3`
will have `versionCode=16908293` (or in hex `0x01020005`).

Builds are always considered to be _snapshot_ builds unless the `buildNumber`
property or the `BUILD_NUMBER` environment variables are specified.

