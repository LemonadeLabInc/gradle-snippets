package de.lemona.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
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
          // cf. https://developer.android.com/studio/build/maven-publish-plugin
          afterEvaluate {
            publishing {
              publications {
                def _group = group
                def _name = name
                def _version = version
                release(MavenPublication) {
                  from components.release

                  groupId = _group
                  artifactId = _name
                  version = _version
                }
              }
            }
          }
        }

        /* ================================================================== */
        /* ANDROID APPLICATION PROJECT PUBLISHING                             */
        /* ================================================================== */

        plugins.withId('com.android.application') {
          // cf. https://developer.android.com/studio/build/maven-publish-plugin
          afterEvaluate {
            publishing {
              publications {
                def _group = group
                def _name = name
                def _version = version
                release(MavenPublication) {
                  from components.release_apk

                  groupId = _group
                  artifactId = _name
                  version = _version
                }
              }
            }
          }
        }
      }
    }
  }
}