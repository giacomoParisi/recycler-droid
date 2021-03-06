import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("maven-publish")
    id("com.jfrog.bintray")

}

android {
    compileSdkVersion(AndroidConfig.compile_sdk)
    defaultConfig {
        minSdkVersion(AndroidConfig.min_sdk)
        targetSdkVersion(AndroidConfig.target_sdk)
        versionCode = AndroidConfig.version_code
        versionName = AndroidConfig.version_name
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
}

dependencies {

    /* --- kotlin --- */
    implementation(Kotlin.StdLib.get())

    /* --- android --- */
    implementation(AndroidX.RecyclerView.get())
    implementation(AndroidX.Paging.Runtime.get())

}

/* ======== BINTRAY ======== */

tasks {

    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(android.sourceSets.getByName("main").java.srcDirs)
    }

    artifacts {
        archives(sourcesJar)
    }
}

val artifactName: String = "${Library.name}-${project.name}"
val artifactGroup: String = Library.group
val artifactVersion: String = AndroidConfig.version_name

publishing {
    publications {
        create<MavenPublication>(Library.name) {

            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion

            artifact("$buildDir/outputs/aar/${project.name}-release.aar")
            artifact(tasks.getByName("sourcesJar"))

            pom {

                packaging = "aar"
                name.set(Library.name)
                description.set(Library.pomDescription)
                url.set(Library.pomUrl)

                licenses {
                    license {
                        name.set(Library.pomLicenseName)
                        url.set(Library.pomLicenseUrl)
                        distribution.set(Library.repo)
                    }
                }

                developers {
                    developer {
                        id.set(Library.pomDeveloperId)
                        name.set(Library.pomDeveloperName)
                        email.set(Library.pomDeveloperEmail)
                    }
                }

                scm {
                    url.set(Library.pomScmUrl)
                }

                withXml {

                    val dependenciesNode = asNode().appendNode("dependencies")

                    (configurations.releaseImplementation.get().allDependencies +
                            configurations.releaseCompile.get().allDependencies)
                            .forEach {

                                val groupId =
                                        if (it.group == rootProject.name) Library.group else it.group

                                val artifactId = it.name

                                val version =
                                        if (it.group == rootProject.name) AndroidConfig.version_name
                                        else it.version

                                if (groupId != null && version != null) {

                                    val dependencyNode =
                                            dependenciesNode.appendNode("dependency")

                                    dependencyNode.appendNode("groupId", groupId)
                                    dependencyNode.appendNode("artifactId", artifactId)
                                    dependencyNode.appendNode("version", version)

                                }

                            }

                }
            }
        }
    }
}

bintray {

    user = gradleLocalProperties(rootDir).getProperty("bintray.user").toString()
    key = gradleLocalProperties(rootDir).getProperty("bintray.apikey").toString()
    publish = true

    setPublications(Library.name)

    pkg.apply {

        repo = Library.repo
        name = artifactName
        githubRepo = Library.githubRepo
        vcsUrl = Library.pomScmUrl
        description = Library.pomDescription
        setLabels("recyclerview", "android", "recycler", "list", "items")
        setLicenses(Library.pomLicenseName)
        desc = Library.pomDescription
        websiteUrl = Library.pomUrl
        issueTrackerUrl = Library.pomIssueUrl
        githubReleaseNotesFile = Library.githubReadme

        version.apply {
            name = artifactVersion
            desc = Library.pomDescription
            vcsTag = artifactVersion
            gpg.sign = true
            gpg.passphrase = gradleLocalProperties(rootDir).getProperty("bintray.gpg.password")
        }
    }
}

