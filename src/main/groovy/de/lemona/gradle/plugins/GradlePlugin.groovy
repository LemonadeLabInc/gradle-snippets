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

        // Create our extension
        project.extensions.create("lemonade", Extension, project)

        // Configure the project
        project.configure(project) {

            Boolean _autoPluginApply = Utilities.resolveValue(project, 'leomo.enableAutoPluginApply', 'LEOMO_ENABLE_AUTO_PLUGIN_APPLY', true).toBoolean()

            if (_autoPluginApply) {
                logger.debug('Start applying suitable plugins')

                // Trigger actions on our plugins
                plugins.withId('java') { project.apply plugin: 'de.lemona.gradle.java' }
                plugins.withId('java-library') { project.apply plugin: 'de.lemona.gradle.java' }
                plugins.withId('maven-publish') { project.apply plugin: 'de.lemona.gradle.publish' }

                plugins.withId('com.android.application') {
                    project.apply plugin: 'de.lemona.gradle.android'
                }
                plugins.withId('com.android.library') {
                    project.apply plugin: 'de.lemona.gradle.android'
                }

                // S3 plugin gets triggered by property/env variable....
                if (Utilities.resolveValue(project, 's3.repository', 'S3_REPOSITORY') != null) {
                    project.apply plugin: 'de.lemona.gradle.s3'
                }
            }

            // Configure the default repositories: google and maven central
            repositories {
                google()
                mavenCentral()
            }

            // Notify out in the build script
            logger.lifecycle('Initialized {}:{} version {} (0x{})', group, name, version, Integer.toHexString(version.versionCode))
        }
    }
}
