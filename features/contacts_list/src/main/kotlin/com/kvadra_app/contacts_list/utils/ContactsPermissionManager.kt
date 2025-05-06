package com.kvadra_app.contacts_list.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class ContactsPermissionManager(
    private val onPermissionGranted: () -> Unit,
    private val onShowRationaleDialog: () -> Unit,
    private val onShowSettingsDialog: () -> Unit,
    private val context: Context
) {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    fun initialize(activityResultCaller: ActivityResultCaller) {
        requestPermissionLauncher = activityResultCaller.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isGranted = permissions.entries.any { it.value }
            if (isGranted) {
                onPermissionGranted()
            } else {
                // Если разрешение не предоставлено, проверяем, нужно ли показать настройки
                if (!shouldShowRequestPermissionRationale()) {
                    onShowSettingsDialog()
                } else {
                    onShowRationaleDialog()
                }
            }
        }
    }

    fun checkAndRequestContactsPermission() {
        when {
            hasContactsPermission() -> {
                onPermissionGranted()
            }
            shouldShowRequestPermissionRationale() -> {
                onShowRationaleDialog()
            }
            else -> requestPermission()
        }
    }

    fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.CALL_PHONE
            )
        )
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    fun hasContactsPermission(): Boolean {
        val readContacts = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val writeContacts = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val callPhone = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        return readContacts && writeContacts && callPhone
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        val activity = context as FragmentActivity
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.READ_CONTACTS
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.WRITE_CONTACTS
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.CALL_PHONE
        )
    }
}