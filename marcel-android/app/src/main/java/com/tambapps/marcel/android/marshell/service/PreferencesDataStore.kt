package com.tambapps.marcel.android.marshell.service

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tambapps.marcel.android.marshell.data.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.subscribe
import java.io.File
import java.io.IOException
import javax.inject.Inject

class PreferencesDataStore @Inject constructor(
  private val dataStore: DataStore<Preferences>
) {

  companion object {
    const val TAG = "PreferencesService"
  }
  private object PreferencesKeys {
    val ASKED_NOTIFICATION_PERMISSIONS = booleanPreferencesKey("asked_notifications_permissions")
    val HOME_DIRECTORY = stringPreferencesKey("home_directory")
  }

  private val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
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

  val askedNotificationPermissionsState
    @Composable
    get() = userPreferencesFlow
    .map { it.askedNotificationPermissions }
    .collectAsState(initial = UserPreferences.DEFAULT.askedNotificationPermissions)

  val homeDirectory
    get() = userPreferencesFlow
      .map { it.homeDirectory }
      .map { if (it != null) File(it) else null }
      .map { if (it?.exists() == true) it else null }


  suspend fun setAskedPermissionsPreferences(value: Boolean) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.ASKED_NOTIFICATION_PERMISSIONS] = value
    }
  }


  suspend fun setHomeDirectory(file: File) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.HOME_DIRECTORY] = file.absolutePath
    }
  }

  private fun mapUserPreferences(preferences: Preferences): UserPreferences {
    return UserPreferences(
      askedNotificationPermissions = preferences[PreferencesKeys.ASKED_NOTIFICATION_PERMISSIONS] ?: UserPreferences.DEFAULT.askedNotificationPermissions,
      homeDirectory = preferences[PreferencesKeys.HOME_DIRECTORY] ?: UserPreferences.DEFAULT.homeDirectory
    )
  }

}