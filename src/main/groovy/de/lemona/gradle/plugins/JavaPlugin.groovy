package de.lemona.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.configure(project) {
      // Source and target compatibility
      sourceCompatibility = '1.7'
      targetCompatibility = '1.7'

      // Add a "provided" configuration
      configurations {
        provided
        provided.extendsFrom(compile)
      }

      // Inject the provided configuration in class paths
      sourceSets {
        main.compileClasspath += configurations.provided
        test.compileClasspath += configurations.provided
        test.runtimeClasspath += configurations.provided
      }

      // Link JavaDOCs properly
      javadoc {
        options.links 'https://docs.oracle.com/javase/7/docs/api/'
        classpath += configurations.provided
      }
    }
  }
}
