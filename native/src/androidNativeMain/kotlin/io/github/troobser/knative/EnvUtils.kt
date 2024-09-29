package io.github.troobser.knative

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.wcstr
import platform.android.JNIEnvVar
import platform.android.jstring

@OptIn(ExperimentalForeignApi::class)
internal class EnvUtils(private val env: CPointer<JNIEnvVar>) {
    private val nativeInterface get() = env.pointed.pointed ?: missingError(N_INTERFACE)

    @OptIn(ExperimentalForeignApi::class)
    fun String.toJString(): jstring {
        memScoped {
            val l = this@toJString.length
            val newStringFun = nativeInterface.NewString ?: missingError(NEW_STR)
            val result = newStringFun.invoke(env, wcstr.ptr, l) ?: nullError(NEW_STR)
            return result
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun jstring.toKString(): String {
        val getUtf = nativeInterface.GetStringUTFChars ?: missingError(GET_UTF)
        val utfResult = getUtf(env, this@toKString, null) ?: nullError(GET_UTF)
        val kotlinStr = utfResult.toKStringFromUtf8()
        val releaseUtf = nativeInterface.ReleaseStringUTFChars ?: missingError(RELEASE_UTF)
        releaseUtf(env, this@toKString, utfResult)
        return kotlinStr
    }

    private fun missingError(name: String): Nothing = throw Exception("jni $name missing")
    private fun nullError(name: String): Nothing = throw Exception("jni $name returned null")

    companion object {
        private const val N_INTERFACE = "nativeInterface"
        private const val NEW_STR = "NewString"
        private const val GET_UTF = "GetStringUTFChars"
        private const val RELEASE_UTF = "ReleaseStringUTFChars"
    }
}