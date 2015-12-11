package de.lemona.gradle.plugins;

import java.io.IOException;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

public class Extension {

    private final Project project;

    public Extension(Project project) {
        if (project == null) throw new GradleException("No project for extension");
        this.project = project;
    }

    public Object requireValue(String propertyName, String envVariableName) {
        return Utilities.requireValue(project, propertyName, envVariableName);
    }

    public Object resolveValue(String propertyName, String envVariableName) {
        return Utilities.resolveValue(project, propertyName, envVariableName);
    }

    public Object resolveValue(String propertyName, String envVariableName, Object defaultValue) {
        return Utilities.resolveValue(project, propertyName, envVariableName, defaultValue);
    }

    public void readProperties(Object fileName)
    throws IOException {
        Utilities.readProperties(project, fileName);
    }

}
