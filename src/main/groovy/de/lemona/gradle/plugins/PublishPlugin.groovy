package de.lemona.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact

class PublishPlugin implements Plugin<Project> {

  // Can't find this in Gradle's APIs and JARs, where is it?
  def Jar = Class.forName('org.gradle.api.tasks.bundling.Jar')
  def MavenPublication = Class.forName('org.gradle.api.publish.maven.MavenPublication')
  def JavaLibrary = Class.forName('org.gradle.api.internal.java.JavaLibrary')

  void apply(Project project) {
    // We just trigger our configurations on version/properties
    project.plugins.withId('maven-publish') {
      project.configure(project) {

        // Our local (test) maven repository
        def _repositoryPath = new File(buildDir, 'maven')

        // Inform where we are publishing to
        logger.lifecycle('Publishing artifacts to \"{}\"', _repositoryPath.toURI())

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
          task publishSourcesJar(type: Jar) {
            classifier = 'sources'
            from sourceSets.main.allSource
          }

          // JavaDoc JAR for publishing
          task publishJavadocJar(type: Jar, dependsOn: javadoc) {
            classifier = 'javadoc'
            from javadoc.destinationDir
          }

          // Create a "java" maven publication
          publishing.publications.create('java', MavenPublication) {
            from components.java
            artifact publishSourcesJar
            artifact publishJavadocJar
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

            // Prepare our publication artifact (from the AAR)
            def _packageTask = tasks['package' + _variantName + 'Jar']
            def _artifact = new ArchivePublishArtifact(_packageTask);
            def _dependencies = configurations.compile.dependencies
            def _component = JavaLibrary.newInstance(_artifact, _dependencies);

            // Also get the AAR itself, to publish alongsite
            def _bundleTask = tasks['bundle' + _variantName]

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
      }
    }
  }
}