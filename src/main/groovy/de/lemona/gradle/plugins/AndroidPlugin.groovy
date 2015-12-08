package de.lemona.gradle.plugins

import org.gradle.api.logging.LogLevel
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc

class AndroidPlugin implements Plugin<Project> {

  void apply(Project project) {
    project.plugins.withId('com.android.application') { configAndroid(project) }
    project.plugins.withId('com.android.library')     { configAndroid(project) }
  }

  def configAndroid = { project ->

    // Global JavaDoc task (dependencies will be added for each variant)
    project.task([group: 'Documentation'], 'javadoc') {
        description 'Generates Javadoc API documentation'
    }

    // Always after evaluation
    project.afterEvaluate {
      project.configure(project) {

      // Load "AndroidTestTask" here, we don't have the android plugin at compile time
      def _androidTestTask = Class.forName('com.android.build.gradle.internal.tasks.AndroidTestTask')

      // Tests logged at "LIFECYCLE" level (this requires hackery)
      tasks.withType(_androidTestTask).each { _task ->
        def _logger = _task.getILogger()
        def _field = _logger.class.getDeclaredField('infoLogLevel');
        _field.setAccessible(true);
        _field.set(_logger, LogLevel.LIFECYCLE)
      }

      // Configure variants for library project
      project.plugins.withId('com.android.library') {
        project.android.libraryVariants.all { variant ->
          configAndroidVariant(project, variant)
        }
      }

      // Configure variants for application project
      project.plugins.withId('com.android.application') {
        project.android.applicationVariants.all { variant ->
          configAndroidVariant(project, variant)
        }
      }
    }
  }}

  def configAndroidVariant = { project, variant ->
    def variantName = variant.name.capitalize()
    project.configure(project) {

        // No "package-list" file exists in Android docs, use our local copy
      def _packageList = getClass().getClassLoader().getResource('javadoc/package-list');

      // Create a "javadoc" task (emulating Java basically)
      tasks.javadoc.dependsOn += task([type: Javadoc, group: 'Documentation'], 'javadoc' + variantName) {
        options.links("http://docs.oracle.com/javase/7/docs/api/")
        description 'Generates '  + variantName  + ' Javadoc API documentation'
        destinationDir = file("$buildDir/docs/javadoc-${variant.name}");
        dependsOn 'compile' + variantName + 'JavaWithJavac'

        // If we have a package list, link to JavaDoc
        if (_packageList != null) {
          def _packageListDir = _packageList.toString().replaceAll(/\/package-list$/, '/');
          options.linksOffline("http://d.android.com/reference/", _packageListDir)
        }

        // Sources to JavaDOC
        source variant.javaCompile.source

        // Start with the "default" Android SDK classpath and add our variant classpath
        classpath = files("${android.sdkDirectory}/platforms/${android.compileSdkVersion}/android.jar")
        classpath += files(variant.javaCompile.classpath.files)

        // Exclude generated stuff
        exclude '**/BuildConfig.java'
        exclude '**/R.java'
      }
    }
  }
}
