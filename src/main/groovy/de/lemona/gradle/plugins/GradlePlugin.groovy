package de.lemona.gradle.plugins

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel

class GradlePlugin implements Plugin<Project> {

  void apply(Project project) {
    // Read out our user and project properties
    Utilities.readUserProperties(project, '.lemonade.properties')
    Utilities.readProperties(project, 'lemonade.properties')

    // Start processing version setup
    Version.setup(project)

    // Configure the project
    project.configure(project) {
      // Trigger actions on our plugins
      plugins.withId('com.android.application') { project.apply plugin:'de.lemona.android' }
      plugins.withId('com.android.library')     { project.apply plugin:'de.lemona.android' }
      plugins.withId('maven-publish')           { project.apply plugin:'de.lemona.publish' }

      // Configure the default repositories: ours, jcenter and maven central
      repositories {
        maven {
          name 'lemonade-oss'
          url 'https://dl.bintray.com/lemonade/maven'
        }
        jcenter()
        mavenCentral()
      }

      // Notify out in the build script
      logger.lifecycle('Initialized {}:{} version {}', group, name, version)
    }
  }
}
