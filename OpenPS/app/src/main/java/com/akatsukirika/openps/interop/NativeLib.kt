package com.akatsukirika.openps.interop

object NativeLib {
    init {
        System.loadLibrary("openps")
    }
}