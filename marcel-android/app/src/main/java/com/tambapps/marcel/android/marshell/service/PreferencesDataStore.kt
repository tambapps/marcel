package com.tambapps.marcel.android.marshell.service

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.tambapps.marcel.android.marshell.data.ShellPreferences
import com.tambapps.marcel.android.marshell.data.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PreferencesDataStore @Inject constructor(
  private val dataStore: DataStore<Preferences>
) {

  companion object {
    const val TAG = "PreferencesService"
  }
  private object PreferencesKeys {
    val SINGLE_LINE_PROMPT = booleanPreferencesKey("single_line_input")
  }

  val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
    .catch { exception ->
      // dataStore.data throws an IOException when an error is encountered when reading data
      if (exception is IOException) {
        Log.e(TAG, "Error reading preferences.", exception)
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }.map { preferences ->
      mapUserPreferences(preferences)
    }

  val shellPreferencesFlow: Flow<ShellPreferences> = userPreferencesFlow.map { userPreferences ->
    userPreferences.shellPreferences
  }

  @Composable
  fun collectShellPreferencesState(): State<ShellPreferences> {
    return shellPreferencesFlow.collectAsState(initial = ShellPreferences.DEFAULT)
  }

  suspend fun setSingleLineInput(value: Boolean) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.SINGLE_LINE_PROMPT] = value
    }
  }

  private fun mapUserPreferences(preferences: Preferences): UserPreferences {
    val singleLineInput = preferences[PreferencesKeys.SINGLE_LINE_PROMPT]
    val shellPreferences = ShellPreferences(singleLineInput ?: ShellPreferences.DEFAULT.singleLineInput)
    return UserPreferences(shellPreferences)
  }

}