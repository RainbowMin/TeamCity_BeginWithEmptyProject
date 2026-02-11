import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.triggers.vcs

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

version = "2024.03"

project {
    buildType(BuildConfigOfEmptyProject)
    sequence {
        build(BuildA)
        build(BuildB) // BuildB has a snapshot dependency on BuildA
        build(BuildC) //
    }
}

object BuildConfigOfEmptyProject : BuildType({
    name = "BuildConfigOfEmptyProject"

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})

/*
为Project类添加了一个扩展函数，使我们能够声明sequence。通过使用前面提到的带接收者的Lambda特性，
我们声明了用作sequence函数参数的代码块将提供Sequence类的上下文
 */
class Sequence {
    val buildTypes = arrayListOf<BuildType>()

    fun build(buildType: BuildType) {
        buildTypes.add(buildType)
    }
}

fun Project.sequence(block: Sequence.()-> Unit){
    val sequence = Sequence().apply(block)
    var previous: BuildType? = null
    // 创建快照依赖
    for (current in sequence.buildTypes){
        if (previous != null){
            current.dependencies.snapshot(previous){}
        }
        previous = current
    }
    // 对每个构建类型调用buildType函数,以将其包含到当前项目中
    sequence.buildTypes.forEach(this::buildType)
}

object BuildA: BuildType({
    name="BuildA"

    steps {
        // define the steps needed to publish the artifacts
    }

    requirements {
        contains("teamcity.agent.name", "minjie")
    }
})

object BuildB: BuildType({
    name="BuildB"

    steps {
        // define the steps needed to publish the artifacts
    }

    requirements {
        contains("teamcity.agent.name", "minjie")
    }
})

object BuildC: BuildType({
    name="BuildC"

    steps {
        // define the steps needed to publish the artifacts
    }

    requirements {
        contains("teamcity.agent.name", "minjie")
    }
})