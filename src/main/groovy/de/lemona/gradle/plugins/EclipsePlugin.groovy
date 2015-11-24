package de.lemona.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class EclipsePlugin implements Plugin<Project> {
  void apply(Project project) {
    project.plugins.withId('eclipse') {
      project.configure(project) {

        // Shared by all Eclipse project types
        eclipse {
          classpath {
            defaultOutputDir = file('.eclipse-out')
            downloadSources = true
            downloadJavadoc = true
          }
        }

        // Optionall add stuff for Android
        plugins.withId('com.android.application') { configAndroid(project) }
        plugins.withId('com.android.library')     { configAndroid(project) }
      }
    }
  }

  def configAndroid = { project ->
    project.configure(project) {

      // Create a "project.properties" for eclipse
      task([group: 'IDE'], 'eclipseAndroid') {
          description 'Generate the local "project.properties" file for Android'
      } << {
        def prop = new Properties()
        prop.target = "${android.compileSdkVersion}".toString()
        def propFile = new File("${projectDir}/project.properties");
        propFile.createNewFile();
        prop.store(propFile.newWriter(), null);
      }

      // Dependencies for various Eclipse tasks
      tasks.eclipse.dependsOn tasks.eclipseAndroid
      tasks.eclipseClasspath.dependsOn tasks.cleanEclipseClasspath

      // Our Eclipse project and classpath
      eclipse.project.natures 'com.android.ide.eclipse.adt.AndroidNature'

      eclipse.classpath {
        sourceSets {
          main {
            java      { srcDirs = android.sourceSets.main.javaDirectories      }
            resources { srcDirs = android.sourceSets.main.resourcesDirectories }
          }
          test {
            java      { srcDirs = android.sourceSets.test.javaDirectories      }
            resources { srcDirs = android.sourceSets.test.resourcesDirectories }
          }
          androidTest {
            java      { srcDirs = android.sourceSets.androidTest.javaDirectories      }
            resources { srcDirs = android.sourceSets.androidTest.resourcesDirectories }
          }
        }

        containers = [ 'com.android.ide.eclipse.adt.ANDROID_FRAMEWORK',
                       'com.android.ide.eclipse.adt.DEPENDENCIES',
                       'com.android.ide.eclipse.adt.LIBRARIES' ]
        plusConfigurations += [ configurations.provided,
                                configurations.compile,
                                configurations.testProvided,
                                configurations.testCompile,
                                configurations.androidTestProvided,
                                configurations.androidTestCompile ]
      }
    }
  }
}
