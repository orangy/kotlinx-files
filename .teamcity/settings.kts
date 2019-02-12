import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.*
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.*

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.2"

val platforms = listOf("Windows", "Linux", "Mac OS X")

project {
    // Disable editing of project and build settings from the UI to avoid issues with TeamCity
    params {
        param("teamcity.ui.settings.readOnly", "true")
    }
    
    buildTypesOrder = platforms.map { build(it) }
}

fun Project.build(platform: String) = BuildType {
    // ID is prepended with Project ID, so don't repeat it here
    // ID should conform to identifier rules, so just letters, numbers and underscore
    id("Build_${platform.substringBefore(" ")}")

    // Display name of the build configuration
    name = "Build ($platform)"

    // Allow to fetch build status through API for badges
    allowExternalStatus = true

    // What files to publish as build artifacts
    artifactRules = """
        +:**/build/libs/*.jar
        +:**/build/libs/*.klib
    """.trimIndent()

    params {
        // This parameter is needed for macOS agent to be compatible
        param("env.JDK_17", "")
    }

    // Configure VCS, by default use the same and only VCS root from which this configuration is fetched
    vcs {
        root(DslContext.settingsRoot)
    }

    // How to build a project
    steps {
        gradle {
            jdkHome = "%env.JDK_18_x64%"
            jvmArgs = "-Xmx1g"
            // --continue is needed to run tests on all platforms even if one platform fails
            tasks = "clean build --continue"
            buildFile = ""
            gradleWrapperPath = ""
        }
    }

    triggers {
        vcs {
            triggerRules = """
                -:*.md
                -:.gitignore
            """.trimIndent()
        }
    }

    features {
        feature {
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "+:**/build/reports/*.xml")
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", platform)
    }
}.also { buildType(it) }




