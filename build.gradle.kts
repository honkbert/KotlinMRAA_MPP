plugins {
    kotlin("multiplatform") version "1.5.0-M2"
    id("maven-publish")
}

group = "com.robgulley"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    configure(listOf(linuxX64())) {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
        val main by compilations.getting
        val mraa by main.cinterops.creating {
//            when (preset) {
////                presets["macosX64"] -> includeDirs.headerFilterOnly("/usr/local/include")
//                presets["linuxX64"] -> includeDirs.headerFilterOnly("/usr/include", "/usr/include/x86_64-linux-gnu")
//            }
        }
    }

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(kotlin("stdlib"))
                implementation ("com.robgulley:vector-lerp:1.0")
            }
        }
        val commonTest by getting
//        val linuxArm64Main by getting
//        val linuxArm64Test by getting
        val linuxX64Main by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
//                implementation ("com.autodesk:coroutineworker:0.6.3")
            }
        }
        val linuxX64Test by getting
//        val macosX64Main by getting
//        val macosX64Test by getting
    }
}
