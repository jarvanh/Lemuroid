package com.swordfish.lemuroid.app.tv.folderpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

class TVFolderPickerLauncher : ImmersiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
            } else {
                startFolderPicker()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            startFolderPicker()
        }
    }

    private fun startFolderPicker() {
        startActivityForResult(Intent(this, TVFolderPickerActivity::class.java), REQUEST_CODE_PICK_FOLDER)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultData: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(this)
            val preferenceKey = getString(com.swordfish.lemuroid.lib.R.string.pref_key_legacy_external_folder)

            val currentValue: String? = sharedPreferences.getString(preferenceKey, null)
            val newValue = resultData?.extras?.getString(TVFolderPickerActivity.RESULT_DIRECTORY_PATH)

            if (newValue.toString() != currentValue) {
                sharedPreferences.edit().apply {
                    this.putString(preferenceKey, newValue.toString())
                    this.commit()
                }
            }

            startLibraryIndexWork()
        }
        finish()
    }

    private fun startLibraryIndexWork() {
        LibraryIndexScheduler.scheduleLibrarySync(applicationContext)
    }

    companion object {
        private const val REQUEST_CODE_PICK_FOLDER = 1
        private const val REQUEST_CODE_PERMISSION = 2

        fun pickFolder(context: Context) {
            context.startActivity(Intent(context, TVFolderPickerLauncher::class.java))
        }
    }
}
