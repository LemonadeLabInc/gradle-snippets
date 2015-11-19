Lemonade Lab Gradle Plugins
===========================

This project contains a set of [Gradle](http://gradle.org/) plugins to
customize the standard build conventions to the ones used at
[Lemonade Lab](http://lemona.de/)

* [Init Plugin](#init-plugin)


Init Plugin
-----------

```groovy
apply plugin: 'de.lemona.init'
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
5. ... TBC




Versioning
----------

The original `version` must be a `String` in the _`major.minor`_ format.

The [Init Plugin](#init-plugin) will parse and overwrite gradle's `version`
field to be an object containing the following fields:

* `major`: the major version number
* `minor`: the minor version number
* `build`: the build number or `-1` if this is a snapshot version
* `versionCode`: the version code (calculated as a 32-bit integer from `major`,
  `minor` and `build`) for release or `1` for snapshot builds
* `isSnapshot`: a flag indicating a release or snapshot build

The final `toString()` representation of the `version` will be either
_`minor.major.build`_ or _`minor.major-SNAPSHOT`_

Builds are always considered to be _snapshot_ builds unless the `buildNumber`
property or the `BUILD_NUMBER` environment variables are specified.

