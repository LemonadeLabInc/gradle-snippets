package de.lemona.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

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
      plugins.withId('java')                    { project.apply plugin:'de.lemona.gradle.java'    }
      plugins.withId('maven-publish')           { project.apply plugin:'de.lemona.gradle.publish' }
      plugins.withId('eclipse')                 { project.apply plugin:'de.lemona.gradle.eclipse' }

      plugins.withId('com.android.application') { project.apply plugin:'de.lemona.gradle.android' }
      plugins.withId('com.android.library')     { project.apply plugin:'de.lemona.gradle.android' }

      plugins.withId('com.jfrog.bintray')       { project.apply plugin:'de.lemona.gradle.bintray' }

      // S3 plugin gets triggered by property/env variable....
      if (Utilities.resolveValue(project, 's3.repository', 'S3_REPOSITORY') != null) {
        project.apply plugin:'de.lemona.gradle.s3'
      }

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
      logger.lifecycle('Initialized {}:{} version {} (0x{})', group, name, version, Integer.toHexString(version.versionCode))
    }
  }
}
