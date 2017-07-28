# Updating individual projects

## Version 0.1.0

The 0.1.0 version of this plugin is fully backward compatible. Simply bumping its version in dependent projects will have no side effects.

However, in order to take advantage of the latest versions of Gradle and the Gradle Android plugin, individual projects will have to do the following updates:

* Gradle -> 4.0.1
* Android Plugin -> 2.3.0
* Android Build Tools -> 25.0.3
* Java -> 1.8

Updating the build tools to 25 will cause linting issues in some projects, so those need to be addressed individually.
