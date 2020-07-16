LEOMO Gradle Plugins
===========================

This project contains a set of [Gradle](http://gradle.org/) plugins to
customize the standard build conventions to the ones used at
[LEOMO, Inc.](http://leomo.io/)

* [Main Plugin](#main-plugin)
* [Android Plugin](#android-plugin)
* [Publishing Plugin](#publishing-plugin)
* [S3 Repository Plugin](#s3-repository-plugin)

Supported Version
-----------------
* Gradle 2.13 - 5.6.4
* Android plugin 3.6.0

In order to take advantage of the latest versions of Gradle and Android, see  [updating.md](updating.md).

Important Note
--------------

Remember that both `group` and `version` **MUST** be specified **BEFORE**
applying the [Main Plugin](#main-plugin).

I do suggest to keep them in the `gradle.properties` file alongside the build
itself.

This is especially evident when applying plugins with the new plugin loading
mechanism, where `plugins` must come before `group` or `version`

```
plugins {
  id 'de.lemona.gradle' version '0.0.1'
}

// Wrong! Specified after the plugin is applied
group 'de.lemona.myproject'
version '1.2'
```


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
4. Set up repositories for dependency resolution:
   * Lemonade's OSS repository on [Bintray](https://bintray.com/lemonade/maven).
   * Bintray's [JCenter](https://bintray.com/lemonade/maven) repository.
   * The [Maven Central](http://search.maven.org/) repository.
   * [Google's Maven](https://maven.google.com/) repository.
5. Set up the rest of our plugins:
   * Add the [Android Plugin](#android-plugin) if either the
     `com.android.application` or `com.android.library` plugins were specified
     in the build file.
   * Add the [Publishing Plugin](#publishing-plugin) if the `maven-publish`
     plugin was specified in the build file.
   * Add the [S3 Repository Plugin](#s3-repository-plugin) if the project has
     the `s3.repository` property or `S3_REPOSITORY` environment variable.
   > <small>_Note:_ if you'd like to avoid using these libraries, set `leomo.enableAutoPluginApply` property (or `LEOMO_ENABLE_AUTO_PLUGIN_APPLY` environment variable) to false. Default true.</small>

The plugin will also inject a `lemonade` extension in the project containing
few utility methods:

* `requireValue(String propertyName, String envVariableName)`
  * Resolve a property name or an environment variable, failing if either/or
    was not defined.
* `resolveValue(String propertyName, String envVariableName)`
  * Resolve a property name or an environment variable, returning `null` if
    both were not defined.
* `resolveValue(String propertyName, String envVariableName, Object defaultValue)`
  * Resolve a property name or an environment variable, returning the specified
    default value if both were not defined.
* `readProperties(Object fileName)`
  * Read a properties file (relative to the project root) injecting the
    properties as `ext` properties to the current project.


Android Plugin
--------------

```groovy
apply plugin: 'de.lemona.gradle.android'
```

This simple plugin will highlight test results in the console output (for
better integration with build systems) and create a `javadoc` task per each
build _variant_ (`debug`, `release`, ...).

It will also simplify the generation of `signingConfigs` by adding a `from`
method in the plugin. For example:

```groovy
signingConfigs {
  from(debug, '../debugKeyStore.jks', 'debugKeyAlias')
  from(release, '../releaseKeyStore.jks', 'releaseKeyAlias')
}
```

The `from(...)` method will take three parameters:

* The **configuration** to sign (required)
* An optional **keystore file** (defaults to `keystore.jks`) relative to the
  project root (if a `String`), or a `File`.
* An optional **key alias** (defaults to the **configuration name**) of the
  key alias in the key store to use.

Few properties and/or environment variables are needed for the configuration:

* `keystorePassword` property or `KEYSTORE_PASSWORD` environment variable
  * The password to decrypt the key store file
* `aliasNameKeyPassword` property or `ALIASNAME_KEY_PASSWORD` environment variable
  * The password the key was encrypted with

In the example above, given the two `debugKeyAlias` and `releaseKeyAlias`
values, the properties searched will be `debugKeyAliasKeyPassword` and
`releaseKeyAliasKeyPassword`, while the environment variable names will be
`DEBUGKEYALIAS_KEY_PASSWORD` and `RELEASEKEYALIAS_KEY_PASSWORD`.



Publishing Plugin
-----------------

```groovy
apply plugin: 'de.lemona.gradle.publishing'
```

This plugin will publish Java and Android artifacts into a local Maven
repository under `${buildDir}/maven`.

For Java artifacts the main java component, sources and javadoc jars will
be published.

For Android _libraries_ the aar file will be published.

For Android _app_ an zip file which contains the apk file and the obfuscation mapping file.

The `release` variant will be published.

S3 Repository Plugin
--------------------

```groovy
apply plugin: 'de.lemona.gradle.s3'
```

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

In this case, use the `uploadS3` task to _actually_ perform the upload (the
`publish` task alone will only publish locally).


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

Publishing
----------

Simply get the API keys after logging in on the Gradle Plugins website then:

```console
$ gradle -PbuildNumber=... \
         -Pgradle.publish.key=... \
         -Pgradle.publish.secret=...
         clean publishPlugins
```
