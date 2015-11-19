package de.lemona.gradle.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

public class Version {

    private final int major;
    private final int minor;
    private final int build;

    public Version(int major, int minor, int build) {
        if ((major < 0) || (major > 127)) throw new GradleException("Major version \"" + major + "\" must be between 0 and 127");
        if ((minor < 0) || (minor > 255)) throw new GradleException("Minor version \"" + minor + "\" must be between 0 and 255");
        if ((build < -1) || (build > 65533)) throw new GradleException("Build number \"" + minor + "\" must be between 0 and 65533");

        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getBuild() {
        return this.build;
    }

    public int getVersionCode() {
        if (this.build < 0) return 1;
        return 2 + (((this.major << 24) & 0x07f000000) // max 127
                  | ((this.minor << 16) & 0x000ff0000) // max 255
                  | (this.build & 0x00000ffff));     // max 65534
    }

    public boolean getIsSnapshot() {
        return this.build < 0;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder().append(major).append('.').append(minor);
        if (build < 0) builder.append("-SNAPSHOT");
        else builder.append('.').append(minor);
        return builder.toString();
    }

    /* ====================================================================== */

    public static Version setup(Project project) {

        // If the version is null or the string "unspecified" we have nothing
        if ((project.getVersion() == null) || "unspecified".equals(project.getVersion())) {
            final Version version = new Version(0, 0, -1);
            project.setVersion(version);
            return version;
        }

        // If already a Lemonade version return that, or fail if not String
        if (project.getVersion() instanceof Version) return (Version) project.getVersion();
        final String projectVersion = project.getVersion().toString();

        // Start checking versions: if we have no MAJOR.MINOR then we have a wrong version
        final Matcher versionMatcher = Pattern.compile("(\\d+)\\.(\\d+)").matcher(projectVersion);
        if (! versionMatcher.matches()) {
            throw new GradleException("Invalid project version \"" + projectVersion + "\"");
        }

        // Parse out the build number from environment/properties
        final String buildNumber = Utilities.resolveValue(project, "buildNumber", "BUILD_NUMBER", "-1").toString();
        if (! buildNumber.matches("-?\\d+")) {
            throw new GradleException("Invalid build number \"" + buildNumber + "\"");
        }

        // Parse and check major, minor as integers
        final int major = Integer.parseInt(versionMatcher.group(1));
        final int minor = Integer.parseInt(versionMatcher.group(2));
        final int build = Integer.parseInt(buildNumber);

        // Re-inject and return our version
        final Version version = new Version(major, minor, build);
        project.setVersion(version);
        return version;
    }
}
