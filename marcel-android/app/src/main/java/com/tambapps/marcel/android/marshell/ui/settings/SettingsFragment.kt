package com.tambapps.marcel.android.marshell.ui.settings

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.service.PermissionHandler
import com.tambapps.marcel.dumbbell.Dumbbell
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        filesPermissionPreference.isChecked = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          val uri = Uri.fromParts("package", requireContext().packageName, null)
          requireContext().startActivity(
            Intent(
              Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
              uri
            ))
        } else {
          Toast.makeText(requireContext(), "Please go to app settings to disable permission", Toast.LENGTH_SHORT).show()
        }
      } else {
        permissionHandler.requestFilesPermission(requireActivity(), requestPermissionLauncher)
      }
      true
    }
    val clearDumbbellsPreference = findPreference<Preference>(getString(R.string.clear_dumbbells_preference_key))!!
    clearDumbbellsPreference.setOnPreferenceClickListener {
      AlertDialog.Builder(requireContext())
        .setTitle("Remove all dumbbells?")
        .setMessage("Maven dependencies into your local repository will be deleted. You will still be able to fetched them again if needed")
        .setNeutralButton("cancel", null)
        .setPositiveButton("yes") { dialogInterface: DialogInterface, i: Int ->
          Dumbbell.deleteAll()
          Toast.makeText(
            requireContext(),
            "All fetched dumbbells was successfully deleted",
            Toast.LENGTH_SHORT
          ).show()
          refreshDumbbellsCount(clearDumbbellsPreference)
        }
      true
    }

    refreshDumbbellsCount(clearDumbbellsPreference)
  }

  private fun refreshDumbbellsCount(clearDumbbellsPreference: Preference) {
    CoroutineScope(Dispatchers.IO).launch {
      val dumbbellsCount = Dumbbell.enumerateDumbbells()
        .map { versionedArtifacts -> versionedArtifacts.value.map { it.value.size }.sum() }
        .sum()
      withContext(Dispatchers.Main) {
        clearDumbbellsPreference.summary = getString(R.string.remove_all_fetched_dumbbells_n, dumbbellsCount)
      }
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