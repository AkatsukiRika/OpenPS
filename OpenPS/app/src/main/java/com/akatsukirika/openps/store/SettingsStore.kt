package com.akatsukirika.openps.store

import com.tangping.kotstore.support.KotStoreFlowModel

object SettingsStore : KotStoreFlowModel<SettingsStore>() {
    override val kotStoreName: String
        get() = "settings_store"

    override val syncSaveAllProperties: Boolean
        get() = true

    var isDebugMode by booleanStore(key = "is_debug_mode")
}