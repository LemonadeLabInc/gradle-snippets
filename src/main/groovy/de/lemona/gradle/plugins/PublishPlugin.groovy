package de.lemona.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact
import org.gradle.api.internal.java.JavaLibrary
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

class PublishPlugin implements Plugin<Project> {

  void apply(Project project) {
    // We just trigger our configurations on version/properties
    project.plugins.withId('maven-publish') {
      project.configure(project) {

        // Our local (test) maven repository
        def _repositoryPath = new File(buildDir, 'maven')

        // Inform where we are publishing to
        logger.info('Publishing artifacts to \"{}\"', _repositoryPath.toURI())

        // Inject our repository
        publishing {
          repositories {
            maven {
              url _repositoryPath
            }
          }
        }

        /* ================================================================== */
        /* JAVA PROJECT PUBLISHING                                            */
        /* ================================================================== */

        plugins.withId('java') {

          // Sources JAR for publishing
          task([type: Jar], 'publishSourcesJar') {
            classifier = 'sources'
            from sourceSets.main.allSource
          }

          // JavaDoc JAR for publishing
          task([type: Jar, dependsOn: javadoc], 'publishJavadocJar') {
            classifier = 'javadoc'
            from javadoc.destinationDir
          }

          // Create a "java" maven publication *AFTER* the project has been
          // evaluated. If we do this now, dependencies will not be included...
          afterEvaluate {
            publishing.publications.create('java', MavenPublication) {
              from components.java
              artifact publishSourcesJar
              artifact publishJavadocJar
            }
          }
        }

        /* ================================================================== */
        /* ANDROID LIBRARY PROJECT PUBLISHING                                 */
        /* ================================================================== */

        plugins.withId('com.android.library') {

          // By default we publish the "release" variant
          def _variant = Utilities.resolveValue(project, 'publishVariant', 'PUBLISH_VARIANT', 'release')
          def _variantName = _variant.capitalize()

          // Process all the library variants
          android.libraryVariants.all { variant ->
            if (variant.name != _variant) {
              logger.info('Skipping publishing of "{}" variant', variant.name)
              return;
            }

            // Sources JAR for publishing
            def _publishSourcesJar = task([type: Jar], 'publish' + _variantName + 'SourcesJar') {
              classifier = 'sources'
              from android.sourceSets.main.javaDirectories
              from android.sourceSets.main.resourcesDirectories
            }

            def _jarTaskName = 'package' + _variantName + 'Jar'
            if (tasks.findByName(_jarTaskName) == null) {
              println 'No Jar task found, creating one: ' + _jarTaskName
              task([type: Jar], _jarTaskName) {
                dependsOn variant.javaCompiler
                from variant.javaCompiler.destinationDir
                exclude '**/R.class', '**/R$*.class', '**/R.html', '**/R.*.html'
              }
            }

            // Prepare our publication artifact (from the AAR)
            def _packageTask = tasks[_jarTaskName]
            def _artifact = new ArchivePublishArtifact(_packageTask);
            def _dependencies = configurations.compile.dependencies
            def _component = new JavaLibrary(_artifact, _dependencies);

            // Also get the AAR itself, to publish alongsite
            def _bundleTask = tasks['bundle' + _variantName + 'Aar']

            // Publish in our maven repository
            def _versionCode = version.versionCode
            publishing.publications.create(_variant, MavenPublication) {
              from _component
              artifact _bundleTask
              artifact _publishSourcesJar
              pom {
                packaging = 'jar'
                withXml {
                  asNode().appendNode('properties')
                          .appendNode('versionCode', _versionCode)
                }
              }
            }

            // The "javadocVariantName" task might not be here quite just yet
            tasks.all { _javadocTask ->
              // Ignore if not named "javadocVariantName"
              if (_javadocTask.name != 'javadoc' + _variantName) return

              // Create a "publishVariantNameJavadocJar" task from the _javadocTask
              def _publishJavadocJar = task([type: Jar], 'publish' + _variantName + 'JavadocJar') {
                dependsOn _javadocTask
                classifier = 'javadoc'
                from _javadocTask.destinationDir
              }

              // Add the Javadoc JAR artifact to our publication
              publishing.publications.getByName(_variant) { publication ->
                publication.artifact _publishJavadocJar
              }
            }
          }
        }

        /* ================================================================== */
        /* ANDROID APPLICATION PROJECT PUBLISHING                             */
        /* ================================================================== */

        plugins.withId('com.android.application') {

          // By default we publish the "release" variant
          def _variant = Utilities.resolveValue(project, 'publishVariant', 'PUBLISH_VARIANT', 'release')
          def _variantName = _variant.capitalize()

          // Process all the application variants
          android.applicationVariants.all { variant ->
            if (variant.name != _variant) {
              logger.info('Skipping publishing of "{}" variant', variant.name)
              return;
            }

            // The package task (creates APK) and version code
            def _packageTask = tasks['package' + _variantName]
            def _versionCode = version.versionCode

            // Make sure we *depend* on the package task
            tasks['publish'].dependsOn _packageTask

            // Create our APK publication
            publishing.publications.create(_variant, MavenPublication) {
              _packageTask.outputs.files.each {
                logger.debug("debugInfo: output files {}", it.toString())
                def files = it.listFiles(new FileFilter() {
                  @Override
                  boolean accept(File file) {
                    if (file.name.endsWith(".apk") && file.name.contains(_variant)) {
                      return true;
                    }
                    return false
                  }
                })
                if (files != null && files.length > 0) {
                  File apk = files[0]
                  artifact apk.absolutePath
                  pom {
                    packaging = 'apk'
                    withXml {
                      asNode().appendNode('properties')
                              .appendNode('versionCode', _versionCode)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  def canBeResolved(configuration) {
    // isCanBeResolved() was introduced with Gradle 3.3 so check for its existence first
    configuration.metaClass.respondsTo(configuration, "isCanBeResolved") ?
            configuration.isCanBeResolved() : true
  }
}