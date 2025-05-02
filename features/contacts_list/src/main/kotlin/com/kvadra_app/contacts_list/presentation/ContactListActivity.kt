package com.kvadra_app.contacts_list.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.kvadra_app.contacts_list.R
import com.kvadra_app.contacts_list.data.Contact
import com.kvadra_app.contacts_list.data.ContactItem
import com.kvadra_app.contacts_list.databinding.ActivityContactListBinding
import com.kvadra_app.contacts_list.di.ContactsListDepsProvider
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class ContactListActivity : AppCompatActivity() {
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

        initDI()
        if (checkPermissions()) {
            loadContacts()
        } else {
            requestPermissions()
        }
    }

    private fun initDI() {
        val contactsListComponent =
            (applicationContext as ContactsListDepsProvider).getContactsListComponent()
        contactsListComponent.inject(this)
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
        return readContacts && writeContacts
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
            )
        )
    }

    private fun loadContacts() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val contacts = getContacts()
        val contactItems = groupContactsByLetter(contacts)

        val adapter = ContactsAdapter(contactItems)
        recyclerView.adapter = adapter
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

                    if (nameIndex != -1 && phoneNumberIndex != -1) {
                        val name = it.getString(nameIndex)
                        val phoneNumber = it.getString(phoneNumberIndex)
                        contacts.add(Contact(name, phoneNumber))
                    }
                } while (it.moveToNext())
            }
        }
        return contacts
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
}