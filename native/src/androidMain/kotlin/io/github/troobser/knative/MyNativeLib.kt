package io.github.troobser.knative

object MyNativeLib {
    init {
        System.loadLibrary("android")
        System.loadLibrary("KotlinNative")
    }
    external fun nativeGreeting(name: String): String
}