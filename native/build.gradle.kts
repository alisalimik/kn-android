import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

private val Project.jniSourceRoot: File
    get() = File(
        layout.buildDirectory.asFile.get(), "generated/jniLibs"
    )
val nativeFilters: List<String> = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")

kotlin {
    val android = androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class) compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    applyDefaultHierarchyTemplate()
    listOf(
        androidNativeArm64(), androidNativeArm32(), androidNativeX64(), androidNativeX86()
    ).forEachIndexed { index, target ->
        target.binaries {
            sharedLib {
                baseName = "KotlinNative"
                val allowedBuildMode = if (buildType == DEBUG) "debug" else "release"
                val afterLink = addsNativeLibToJniSources(this@sharedLib, nativeFilters[index])
                android.compilations.all {
                    if (name == allowedBuildMode) {
                        @Suppress("DEPRECATION") compileTaskProvider.dependsOn(afterLink)
                    }
                }
            }
        }
    }
    sourceSets {
        commonMain.dependencies {}
        androidNativeMain.dependencies {}
    }
}
android {
    namespace = "io.github.troobser.knative"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("jniLibs", jniSourceRoot)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

private fun Project.addsNativeLibToJniSources(
    library: SharedLibrary, abi: String
): KotlinNativeLink {
    val linkTask = library.linkTaskProvider.get()
    linkTask.doLast {
        copy {
            from(library.outputFile)
            into(File(jniSourceRoot.absolutePath + "/$abi"))
        }
    }
    return linkTask
}
