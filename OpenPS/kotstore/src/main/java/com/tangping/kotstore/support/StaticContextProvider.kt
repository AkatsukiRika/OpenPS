package com.tangping.kotstore.support

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
internal object StaticContextProvider : ContextProvider {
    private var staticContext: Context? = null

    override fun getApplicationContext(): Context? {
        return staticContext
    }

    fun setContext(context: Context) {
        staticContext = context
    }
}