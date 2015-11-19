package de.lemona.gradle.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.ExtraPropertiesExtension;

public final class Utilities {

    private Utilities() {
        throw new IllegalStateException("Do not construct");
    }

    /* ====================================================================== */

    public static Object requireValue(Project project, String propertyName, String envVariableName) {
        final Object value = resolveValue(project, propertyName, envVariableName, null);
        if (value != null) return value;
        throw new GradleException("Required property \"" + propertyName +
               "\" or environment variable \"" + envVariableName + "\" missing");
    }

    public static Object resolveValue(Project project, String propertyName, String envVariableName) {
        return resolveValue(project, propertyName, envVariableName, null);
    }

    public static Object resolveValue(Project project, String propertyName, String envVariableName, Object defaultValue) {
        if (project.hasProperty(propertyName)) return project.property(propertyName);

        final String envVariableValue = System.getenv(envVariableName);
        if (envVariableValue != null) return envVariableValue;

        return defaultValue;
    }

    /* ====================================================================== */

    public static void readUserProperties(Project project, String fileName)
    throws IOException {
        if (fileName == null) throw new GradleException("Properties file name is null");
        _readProperties(project, new File(System.getProperty("user.home"), fileName));
    }


    public static void readProperties(Project project, Object fileName)
    throws IOException {
        if (fileName == null) throw new GradleException("Properties file name is null");

        // Read the base properties relative to the project
        _readProperties(project, project, fileName);

        // If we have a root project, read the parent properties too
        final Project rootProject = project.getRootProject();
        if (! project.equals(rootProject)) {
            _readProperties(rootProject, project, fileName);
        }
    }

    private static void _readProperties(Project source, Project target, Object fileName)
    throws IOException {
        _readProperties(target, source.file(fileName));
    }

    private static void _readProperties(Project project, File propertiesFile)
    throws IOException {
        final Logger logger = project.getLogger();

        if (! propertiesFile.isFile()) {
            logger.debug("Proptertis file \"{}\" not found, ignoring...", propertiesFile);
            return;
        }

        final ExtraPropertiesExtension ext = (ExtraPropertiesExtension) project.getExtensions().getByName("ext");

        logger.lifecycle("Parsing properties from \"{}\"", propertiesFile);
        final Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));

        for (final Object object: properties.keySet()) {
            final String key = (String) object;
            if (project.hasProperty(key)) continue;
            ext.set(key, properties.get(key));
        }
    }

}
