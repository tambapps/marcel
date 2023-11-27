package com.tambapps.marcel.android.marshell.service

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionHandler @Inject constructor() {

  fun requestFilesPermission(context: Activity, requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
    if (hasManageFilesPermission()) {
      val uri = Uri.fromParts("package", context.packageName, null)
      context.startActivity(
        Intent(
          Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
          uri
        )
      )
    } else {
      requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))
    }
  }

  private fun hasManageFilesPermission(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
  }

  fun hasFilesPermission(context: Context): Boolean {
    return if (hasManageFilesPermission()) Environment.isExternalStorageManager()
    else hasAndroidPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        && hasAndroidPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
  }

  private fun hasAndroidPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
  }
}