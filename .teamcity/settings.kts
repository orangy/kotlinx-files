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
    buildTypesOrder = platforms.map { build(it) }
}

fun Project.build(platform: String) = BuildType {
    id("kotlinx_files_build_${platform.substringBefore(" ")}")
    name = "Build ($platform)"
    allowExternalStatus = true
    artifactRules = """
        +:**/build/libs/*.jar
        +:**/build/libs/*.klib
    """.trimIndent()

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            tasks = "clean build"
            buildFile = ""
            gradleWrapperPath = ""
        }
        gradle {
            tasks = "verifyPublications"
            gradleParams = "--stacktrace"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
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




