package com.timemark.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * 顶层 Context 扩展属性，全局仅持有一个 Preferences DataStore 实例。
 * DataStore 名称："settings"。
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
