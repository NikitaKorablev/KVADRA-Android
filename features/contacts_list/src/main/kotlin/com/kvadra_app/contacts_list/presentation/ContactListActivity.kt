package com.kvadra_app.contacts_list.presentation

import android.Manifest
import android.content.ComponentName
import android.content.ContentProviderOperation
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.kvadra_app.contacts_list.domain.OnContactClickListener
import androidx.core.net.toUri
import com.aidl.AidlException
import com.aidl.ContactsList
import com.aidl.RemoveDuplicateContacts
import com.aidl.ResultCallback
import com.kvadra_app.contacts_list.domain.ContactsRemovingStatus

class ContactListActivity : AppCompatActivity(), OnContactClickListener {
    private lateinit var binding: ActivityContactListBinding
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            loadContacts()
        } else {
            // Обработка случая, когда разрешения не предоставлены
        }
    }

    private var removeDuplicateContacts: RemoveDuplicateContacts? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            removeDuplicateContacts = RemoveDuplicateContacts.Stub.asInterface(service)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            removeDuplicateContacts = null
        }
    }

    private lateinit var contactsList: ContactsList

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

        if (checkPermissions()) {
            loadContacts()
        } else {
            requestPermissions()
        }

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
        removeDuplicateContacts?.execute(contactsList, object : ResultCallback.Stub() {
            override fun onSuccess(contacts: ContactsList) {
                when (val res = deleteContacts(contacts.contacts)) {
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
                showToast(ContactsRemovingStatus.REMOVING_EXCEPTION)
                Log.e(TAG, aidlException.toException().message.toString())
            }
        })
    }

    private fun checkPermissions(): Boolean {
        val readContacts = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val writeContacts = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val callPhone = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        return readContacts && writeContacts && callPhone
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.CALL_PHONE
            )
        )
    }

    private fun loadContacts() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val contacts = getContacts()
        contactsList = ContactsList(contacts)
        val contactItems = groupContactsByLetter(contacts)

        val adapter = recyclerView.adapter as? ContactsAdapter
        if (adapter != null) {
            adapter.updateContacts(contactItems)
        } else {
            recyclerView.adapter = ContactsAdapter(contactItems, this)
        }
    }

    private fun getContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val nameIndex = it.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneNumberIndex = it.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val contactIdIndex = it.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val rawContactIdIndex = it.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)

                    if (nameIndex != -1 && phoneNumberIndex != -1 &&
                        contactIdIndex != -1 && rawContactIdIndex != -1) {

                        val name = it.getString(nameIndex) ?: ""
                        val phoneNumber = it.getString(phoneNumberIndex) ?: ""
                        val contactId = it.getLong(contactIdIndex)
                        val rawContactId = it.getLong(rawContactIdIndex)
                        contacts.add(Contact(name, phoneNumber, contactId, rawContactId))
                    }
                } while (it.moveToNext())
            }
        }
        return contacts
    }

    private fun deleteContacts(contacts: List<Contact>): ContactsRemovingStatus {
        if (contacts.isEmpty())
            return ContactsRemovingStatus.Success(ContactsRemovingStatus.NO_DUPLICATE_CONTACTS)

        val ops = ArrayList<ContentProviderOperation>()
        for (contact in contacts) {
            Log.d(TAG, "Deleting contact: ${contact.name}, RawContactId: ${contact.rawContactId}")
            ops.add(
                ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.RawContacts._ID}=?",
                        arrayOf(contact.rawContactId.toString())
                    )
                    .build()
            )
        }

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            return ContactsRemovingStatus.Success(ContactsRemovingStatus.SUCCESS_MESSAGE)
        } catch (e: Exception) {
            return ContactsRemovingStatus.Failed(
                ContactsRemovingStatus.UNEXPECTED_EXCEPTION,
                e.message.toString()
            )
        }
    }

    private fun groupContactsByLetter(contacts: List<Contact>): List<ContactItem> {
        val contactItems = mutableListOf<ContactItem>()
        var currentHeader: Char? = null

        for (contact in contacts) {
            val header = contact.name.first().uppercaseChar()
            if (header != currentHeader) {
                currentHeader = header
                contactItems.add(ContactItem(null, currentHeader))
            }
            contactItems.add(ContactItem(contact, null))
        }
        return contactItems
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