package com.akatsukirika.openps.store

import com.tangping.kotstore.support.KotStoreFlowModel

object SettingsStore : KotStoreFlowModel<SettingsStore>() {
    const val PHOTO_SIZE_NO_LIMIT = 0
    const val PHOTO_SIZE_LIMIT_4K = 1
    const val PHOTO_SIZE_LIMIT_2K = 2
    const val PHOTO_SIZE_LIMIT_1K = 3

    override val kotStoreName: String
        get() = "settings_store"

    override val syncSaveAllProperties: Boolean
        get() = true

    var isDebugMode by booleanStore(key = "is_debug_mode")
    var photoSizeLimit by intStore(key = "photo_size_limit", default = PHOTO_SIZE_NO_LIMIT)
}