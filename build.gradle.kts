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
        val commonMain by getting {
            dependencies {
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
//                implementation("org.jetbrains.kotlinx:atomicfu:0.15.1")
                api("co.touchlab:stately-isolate:1.1.4-a1")
                implementation("com.robgulley:vector-lerp:1.0")
            }
        }

        val desktopMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib"))
//                api("co.touchlab:stately-isolate:1.1.4-a1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC-native-mt")
//                implementation("org.jetbrains.kotlinx:atomicfu:0.15.1")
            }
        }
        val desktopTest by creating {}

//        val linuxArm64Main by getting
//        val linuxArm64Test by getting

        val linuxX64Main by getting {
            dependsOn(desktopMain)
            dependencies {
//                api("co.touchlab:stately-isolate:1.1.4-a1")
            }
        }
        val linuxX64Test by getting

        val macosX64Main by getting {
            dependsOn(desktopMain)
        }
        val macosX64Test by getting
    }
}