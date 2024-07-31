package com.tangping.kotstore.support

import android.content.Context

interface ContextProvider {
    fun getApplicationContext(): Context?
}