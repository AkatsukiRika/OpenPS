package com.akatsukirika.openps

import android.app.Application
import com.tangping.kotstore.support.KotStore

class OpenPSApplication : Application() {
    companion object {
        var instance: OpenPSApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        KotStore.init(this)
        instance = this
    }
}