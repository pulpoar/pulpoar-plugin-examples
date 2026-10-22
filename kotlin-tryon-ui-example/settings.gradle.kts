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
        // The PulpoAR SDK is served from the PulpoAR CDN, not from Maven Central.
        maven {
            url = uri("https://assets.pulpoar.com/vision/pulpo-sdk/android/maven")
            content { includeGroup("com.pulpoar") }
        }
    }
}

rootProject.name = "kotlin-tryon-ui-example"

include(":app")
