package io.github.troobser.knative

@Suppress("unused")
object MyNativeLibImpl {
    fun nativeGreeting(name: String): String {
        return "Hello $name!"
    }
}