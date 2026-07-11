pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "ConnectPremium"
include(":app")
include(":core")
include(":data")
include(":domain")
include(":ui")
include(":bluetooth")
include(":watch")
include(":ai")
include(":health")
include(":sport")
include(":notifications")
include(":camera")
include(":battery")
include(":weather")
include(":calendar")
include(":settings")
include(":backup")
include(":diagnostics")
