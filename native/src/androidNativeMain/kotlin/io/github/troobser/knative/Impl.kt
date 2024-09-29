package io.github.troobser.knative

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.android.JNIEnvVar
import platform.android.jobject
import platform.android.jstring
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
@CName("Java_io_github_troobser_knative_MyNativeLib_nativeGreeting")
fun nGreetingJni(env: CPointer<JNIEnvVar>, thiz: jobject, name: jstring): jstring {
    val utils = EnvUtils(env)
    val kName = with(utils) { name.toKString() }
    val result = MyNativeLibImpl.nativeGreeting(kName)
    return with(utils) { result.toJString() }
}