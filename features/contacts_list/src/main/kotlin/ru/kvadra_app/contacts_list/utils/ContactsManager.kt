package ru.kvadra_app.contacts_list.utils

import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import ru.kvadra_app.contacts_list.R
import ru.kvadra_app.model.Contact
import ru.kvadra_app.model.ContactItem
import ru.kvadra_app.model.ResultState

class ContactsManager(
  private val context: Context
) {
    fun getContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val cursor: Cursor? = context.contentResolver.query(
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

    fun deleteContacts(contacts: List<Contact>): ResultState<String, String> {
        if (contacts.isEmpty())
            return ResultState.Success(context.getString(R.string.no_duplicate_contacts))

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
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            return ResultState.Success(context.getString(R.string.success_message))
        } catch (e: Exception) {
            val err = context.getString(R.string.unexpected_exception)
            Log.e(TAG, err, e)

            return ResultState.Error(err)
        }
    }

    fun groupContactsByLetter(contacts: List<Contact>): List<ContactItem> {
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

    companion object {
        private const val TAG = "ContactsManager"
    }
}