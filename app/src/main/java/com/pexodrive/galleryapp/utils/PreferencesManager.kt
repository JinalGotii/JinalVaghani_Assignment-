package com.pexodrive.galleryapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pexodrive.galleryapp.utils.Constants.GRID_COLUMNS_DEFAULT
import com.pexodrive.galleryapp.utils.Constants.KEY_GRID_COLUMNS
import com.pexodrive.galleryapp.utils.Constants.PREFERENCES_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gridColumnsKey = intPreferencesKey(KEY_GRID_COLUMNS)

    val gridColumnCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[gridColumnsKey] ?: GRID_COLUMNS_DEFAULT
    }

    suspend fun setGridColumnCount(columns: Int) {
        context.dataStore.edit { preferences ->
            preferences[gridColumnsKey] = columns
        }
    }
}
