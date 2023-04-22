package com.tambapps.marcel.android.marshell.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.service.PermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: PreferenceFragmentCompat() {

  @Inject
  lateinit var permissionHandler: PermissionHandler
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
  private lateinit var filesPermissionPreference: SwitchPreference

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
      if (it.values.all { it }) {
        Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(requireContext(), "Some permission weren't granted", Toast.LENGTH_SHORT).show()
      }
      updatePermissionPreferences()
    }
  }

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.settings_preferences, rootKey)
    filesPermissionPreference = findPreference(getString(R.string.files_permission_key))!!
    filesPermissionPreference.setOnPreferenceClickListener {
      if (permissionHandler.hasFilesPermission(requireContext())) {
        Toast.makeText(requireContext(), "Please go to app settings to disable permission", Toast.LENGTH_SHORT).show()
      } else {
        permissionHandler.requestFilesPermission(requireActivity(), requestPermissionLauncher)
      }
      true
    }
  }

  override fun onResume() {
    super.onResume()
    updatePermissionPreferences()
  }

  private fun updatePermissionPreferences() {
    filesPermissionPreference.isChecked = permissionHandler.hasFilesPermission(requireContext())
  }

}