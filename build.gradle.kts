plugins {
    kotlin("multiplatform") version "1.5.0"
    id("maven-publish")
}

group = "com.robgulley"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    macosX64()
    linuxX64 {
        binaries {
            executable {
                entryPoint = "main_foo"
            }
        }
        val main by compilations.getting
        val mraa by main.cinterops.creating {}
    }

    sourceSets {
        val desktopMain by creating {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("com.robgulley:vector-lerp:1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC-native-mt")
            }
        }
        val desktopTest by creating {}

//        val linuxArm64Main by getting
//        val linuxArm64Test by getting

        val linuxX64Main by getting {
            dependsOn(desktopMain)
//            dependencies {
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
//            }
        }
        val linuxX64Test by getting

        val macosX64Main by getting {
            dependsOn(desktopMain)
        }
        val macosX64Test by getting
    }
}