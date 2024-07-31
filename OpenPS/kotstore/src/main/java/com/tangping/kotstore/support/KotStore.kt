package com.tangping.kotstore.support

import android.content.Context
import android.os.Build

object KotStore {
    var useProtectedContext: Boolean = false

    fun init(context: Context) {
        if (useProtectedContext) {
            StaticContextProvider.setContext(getProtectedContext(context))
        } else {
            StaticContextProvider.setContext(context.applicationContext)
        }
    }

    private fun getProtectedContext(context: Context): Context {
        var protectedContext: Context = context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!context.isDeviceProtectedStorage) {
                protectedContext = context.createDeviceProtectedStorageContext()
            }
        }
        return protectedContext
    }
}