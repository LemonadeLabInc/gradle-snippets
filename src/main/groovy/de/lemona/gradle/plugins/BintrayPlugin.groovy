package de.lemona.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class BintrayPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.plugins.withId('com.jfrog.bintray') {
      project.configure(project) {

        def _bintrayUser = Utilities.resolveValue(project, 'bintray.user', 'BINTRAY_USER')
        def _bintrayKey = Utilities.resolveValue(project, 'bintray.key', 'BINTRAY_KEY')
        def _bintrayOrg = Utilities.resolveValue(project, 'bintray.org', 'BINTRAY_ORG')

        // Default Bintray configuration
        bintray {
          user = _bintrayUser
          key = _bintrayKey
          publications = []
          publish = true
          pkg {
            repo = 'maven'
            userOrg = _bintrayOrg
            name = project.group + '.' + project.name
            version {
              name = project.version.toString()
            }
          }
        }

        // Do not upload if this is a snapshot version
        if (version.isSnapshot) bintray.dryRun = true

        // Add all our publications
        project.plugins.withId('maven-publish') {
          publishing.publications.all { publication ->
            bintray.publications += publication.name
          }
        }
      }
    }
  }
}
