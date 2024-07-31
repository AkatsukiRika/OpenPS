package com.akatsukirika.openps

import android.app.Application
import com.tangping.kotstore.support.KotStore

class OpenPSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KotStore.init(this)
    }
}