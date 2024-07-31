package com.tangping.kotstore.support

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.tangping.kotstore.support.store.BooleanStore
import com.tangping.kotstore.support.store.DoubleStore
import com.tangping.kotstore.support.store.FloatStore
import com.tangping.kotstore.support.store.IntStore
import com.tangping.kotstore.support.store.LongStore
import com.tangping.kotstore.support.store.StringStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.properties.ReadWriteProperty

abstract class KotStoreModel(
    private val contextProvider: ContextProvider = StaticContextProvider,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val context: Context?
        get() = contextProvider.getApplicationContext()

    open val kotStoreName: String = ""

    open val syncSaveAllProperties: Boolean = false

    internal val dataStore by lazy {
        check(kotStoreName.isNotEmpty()) {
            "kotStoreName must be set in KotStoreModel"
        }
        check(context != null) {
            "KotStore must be initialized first"
        }
        PreferenceDataStoreFactory.create(
            migrations = listOf(SharedPreferencesMigration(context!!, kotStoreName))
        ) {
            context!!.preferencesDataStoreFile(kotStoreName)
        }
    }

    protected fun stringStore(
        key: String,
        default: String = "",
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, String> = StringStore(key, default, syncSave)

    protected fun intStore(
        key: String,
        default: Int = 0,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Int> = IntStore(key, default, syncSave)

    protected fun longStore(
        key: String,
        default: Long = 0L,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Long> = LongStore(key, default, syncSave)

    protected fun floatStore(
        key: String,
        default: Float = 0f,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Float> = FloatStore(key, default, syncSave)

    protected fun booleanStore(
        key: String,
        default: Boolean = false,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Boolean> = BooleanStore(key, default, syncSave)

    protected fun doubleStore(
        key: String,
        default: Double = 0.0,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Double> = DoubleStore(key, default, syncSave)
}