# Updating individual projects
## Version 0.3.8
The main plugin (`de.lemona.gradle`) automatically applies other plugins based on the build file of dependent projects, as default.

If you'd like to apply these plugins manually, please set the property to `false`.

## Version 0.3.7
0.3.7 supports Gradle 5.
You can use the latest (as 2020) Android gradle plugin in dependent Android projects.
However, the publishing plugin doesn't have a backward compatibility for Android projects.
So, please update the followings:

* Gradle -> 5.6.4
* Android Plugin -> 3.6.0
* Android Build Tools -> 28.0.3 or later

## Version 0.1.0

The 0.1.0 version of this plugin is fully backward compatible. Simply bumping its version in dependent projects will have no side effects.

However, in order to take advantage of the latest versions of Gradle and the Gradle Android plugin, individual projects will have to do the following updates:

* Gradle -> 4.0.1
* Android Plugin -> 2.3.0
* Android Build Tools -> 25.0.3
* Java -> 1.8

Updating the build tools to 25 will cause linting issues in some projects, so those need to be addressed individually.
