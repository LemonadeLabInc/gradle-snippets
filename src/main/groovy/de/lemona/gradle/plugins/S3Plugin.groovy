package de.lemona.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.credentials.AwsCredentials

class S3Plugin implements Plugin<Project> {
  void apply(Project project) {

    // Get our required properties to access S3
    def _repository = Utilities.requireValue(project, 's3.repository', 'S3_REPOSITORY')
    def _accessKey  = Utilities.requireValue(project, 's3.accessKey',  'S3_ACCESS_KEY')
    def _secretKey  = Utilities.requireValue(project, 's3.secretKey',  'S3_SECRET_KEY')

    // Configure the project
    project.configure(project) {

      // Inform of our S3 repository
      logger.info('Using S3 repository at \"{}\"', _repository)

      // Inject our repository in the list
      def _instance = repositories.maven {
        name 'build'
        url _repository
        credentials(AwsCredentials) {
          accessKey _accessKey
          secretKey _secretKey
        }
      }

      // Make sure it's at the top of the list
      repositories.remove(_instance)
      repositories.add(0, _instance)

      // Also *PUBLISH* in the repository if needed
      plugins.withId('maven-publish') {

        // Define the S3 repository to publish to
        publishing {
          repositories {
            maven {
              name 's3'
              url _repository
              credentials(AwsCredentials) {
                accessKey _accessKey
                secretKey _secretKey
              }
            }
          }
        }

        // Create a task to *specifically* upload to S3
        tasks.create(name: 'uploadS3', dependsOn: 'publish')

        // Unless 'uploadS3' is specifically in the task graph, all
        // publishing to S3 is actually disabled
        gradle.taskGraph.whenReady {taskGraph ->
          if (! taskGraph.hasTask(tasks.uploadS3)) {
            taskGraph.allTasks.each { task ->
              if ((task.name =~ '^publish.*ToS3Repository').matches()) {
                logger.warn('Disabling S3 publishing task "{}"', task.name)
                task.enabled = false
              }
            }
          }
        }
      }
    }
  }
}
