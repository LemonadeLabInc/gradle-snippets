package de.lemona.gradle.plugins

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel

class AndroidPlugin implements Plugin<Project> {

  // Can't find this in Gradle's APIs and JARs, where is it?
  def Javadoc = Class.forName('org.gradle.api.tasks.javadoc.Javadoc')

  void apply(Project project) {
    project.plugins.withId('com.android.application') { configAndroid(project) }
    project.plugins.withId('com.android.library')     { configAndroid(project) }
  }

  def configAndroid = { project ->

    // Global JavaDoc task (dependencies will be added for each variant)
    project.task([group: 'Documentation tasks'], 'javadoc') {
        description 'Generates Javadoc API documentation'
    }

    // Always after evaluation
    project.afterEvaluate {
      project.configure(project) {

      // Load "AndroidTestTask" here, in case we don't have the android plugin
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

      // Create a "javadoc" task (emulating Java basically)
      tasks.javadoc.dependsOn += task([type: Javadoc, group: 'Documentation tasks'], 'javadoc' + variantName) {
        options.links("http://docs.oracle.com/javase/7/docs/api/")
        description 'Generates '  + variantName  + ' Javadoc API documentation'
        destinationDir = file("$buildDir/docs/javadoc-${variant.name}");
        dependsOn 'compile' + variantName + 'JavaWithJavac'

        // No "package-list" file exists in Android docs
        // options.links("http://d.android.com/reference/")

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