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
        maven {
            url = file("../repository").toURI()
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "PayPipesExample"
include(":app")
