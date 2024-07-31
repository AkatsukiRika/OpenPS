package com.tangping.kotstore.support.store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey

internal class DoubleStore(
    override val key: String,
    override val default: Double,
    override val syncSave: Boolean
) : AbstractStore<Double>() {
    override fun getPreferencesKey(): Preferences.Key<Double> {
        return doublePreferencesKey(key)
    }
}