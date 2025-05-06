package com.kvadra_app.contacts_list.presentation

import android.app.AlertDialog
import android.content.ComponentName
import android.content.ContentProviderOperation
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.os.Bundle
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.kvadra_app.contacts_list.R
import com.kvadra_app.core.data.Contact
import com.kvadra_app.core.data.ContactItem
import com.kvadra_app.contacts_list.databinding.ActivityContactListBinding
import com.kvadra_app.contacts_list.domain.OnContactClickListener
import androidx.core.net.toUri
import com.aidl.AidlException
import com.aidl.ContactsList
import com.aidl.RemoveDuplicateContacts
import com.aidl.ResultCallback
import com.kvadra_app.contacts_list.domain.ContactsRemovingStatus
import com.kvadra_app.contacts_list.utils.ContactsManager
import com.kvadra_app.contacts_list.utils.ContactsPermissionManager

class ContactListActivity : AppCompatActivity(), OnContactClickListener {
    private lateinit var binding: ActivityContactListBinding
    private lateinit var contactsList: ContactsList

    private lateinit var permissionManager: ContactsPermissionManager
    private lateinit var contactsManager: ContactsManager

    private var removeDuplicateContacts: RemoveDuplicateContacts? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            removeDuplicateContacts = RemoveDuplicateContacts.Stub.asInterface(service)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            removeDuplicateContacts = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        contactsManager = ContactsManager(context = this)
        permissionManager = ContactsPermissionManager(
            onPermissionGranted = { loadContacts() },
            onShowRationaleDialog = { showRationaleDialog() },
            onShowSettingsDialog = { showSettingsDialog() },
            context = this
        )
        permissionManager.initialize(this)
        permissionManager.checkAndRequestContactsPermission()

        binding.serviceButton.setOnClickListener(this::onServiceButtonClick)
    }

    override fun onStart() {
        super.onStart()
        val intent = createExplicitIntent()
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    private fun showToast(message: String) {
        Toast.makeText(this@ContactListActivity, message, Toast.LENGTH_LONG).show()
    }

    private fun onServiceButtonClick(view: View?) {
        if (permissionManager.hasContactsPermission()) {

            removeDuplicateContacts?.execute(contactsList, object : ResultCallback.Stub() {
                override fun onSuccess(contacts: ContactsList) {
                    when (val res = contactsManager.deleteContacts(contacts.contacts)) {
                        is ContactsRemovingStatus.Failed -> {
                            showToast(res.message)
                            Log.e(TAG, res.err)
                        }
                        is ContactsRemovingStatus.Success -> {
                            showToast(res.message)
                            loadContacts()
                        }
                    }
                }

                override fun onError(aidlException: AidlException) {
                    showToast(getString(R.string.removing_exception))
                    Log.e(TAG, aidlException.toException().message.toString())
                }
            })

        } else permissionManager.checkAndRequestContactsPermission()
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required_title))
            .setMessage(getString(R.string.permission_denied_message))
            .setPositiveButton(getString(R.string.permission_grant)) { _, _ ->
                permissionManager.requestPermission()
            }
            .setNegativeButton(getString(R.string.permission_cancel)) { _, _ ->
                showToast(getString(R.string.permission_not_granted))
            }
            .setCancelable(false)
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_denied_title))
            .setMessage(getString(R.string.permission_denied_message))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                permissionManager.openAppSettings()
            }
            .setNegativeButton(getString(R.string.permission_cancel)) { _, _ ->
                showToast(getString(R.string.permission_not_granted))
            }
            .setCancelable(false)
            .show()
    }

    private fun loadContacts() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val contacts = contactsManager.getContacts()
        contactsList = ContactsList(contacts)
        val contactItems = contactsManager.groupContactsByLetter(contacts)

        val adapter = recyclerView.adapter as? ContactsAdapter
        if (adapter != null) {
            adapter.updateContacts(contactItems)
        } else {
            recyclerView.adapter = ContactsAdapter(contactItems, this)
        }
    }

    override fun onContactClick(contact: Contact) {
        Log.d(TAG, "Contact clicked: ${contact.phoneNumber}")
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = "tel:${contact.phoneNumber}".toUri()
        }
        startActivity(intent)
    }

    private fun createExplicitIntent(): Intent {
        val intent = Intent("com.server.service.DuplicateContactsRemoverService")
        val services = packageManager.queryIntentServices(intent, 0)
        if (services.isEmpty()) {
            throw IllegalStateException("Приложение-сервер не установлено")
        }
        return Intent(intent).apply {
            val resolveInfo = services[0]
            val packageName = resolveInfo.serviceInfo.packageName
            val className = resolveInfo.serviceInfo.name
            component = ComponentName(packageName, className)
        }
    }

    companion object {
        const val TAG = "ContactListActivity"
    }
}