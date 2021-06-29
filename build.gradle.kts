plugins {
    kotlin("multiplatform") version "1.5.0"
    id("maven-publish")
}

group = "com.robgulley"
version = "0.1"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm()
    macosX64()
    linuxX64 {
        val main by compilations.getting
        val mraa by main.cinterops.creating {}
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("co.touchlab:stately-isolate:1.1.7-a1")
            }
        }

        val jvmMain by getting { //unused but necessary to make the commonizer think that the commonMain is truly common and not just native-common.
            dependsOn(commonMain)
        }

        val desktopMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("com.robgulley:vector-lerp:1.0")
            }
        }
        val desktopTest by creating {}

//        val linuxArm64Main by getting
//        val linuxArm64Test by getting

        val linuxX64Main by getting {
            dependsOn(desktopMain)
        }
        val linuxX64Test by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-native-mt")
            }
        }

        val macosX64Main by getting {
            dependsOn(desktopMain)
        }
        val macosX64Test by getting
    }
}