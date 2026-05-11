package ru.kvadra_app.server.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import ru.kvadra_app.aidl.RemoveDuplicateContacts
import ru.kvadra_app.aidl.ResultCallback
import ru.kvadra_app.aidl.AidlException
import ru.kvadra_app.aidl.ContactsList
import ru.kvadra_app.model.Contact

class DuplicateContactsRemoverService : Service() {

    private fun removeDuplicateContacts(contactsList: List<Contact>, callback: ResultCallback) {
        val contactsMap = contactsList.groupBy { "${it.name}-${it.phoneNumber}" }

        val duplicatedContacts = mutableListOf<Contact>()
        for (contacts in contactsMap.values) {
            if (contacts.size > 1) {
                for (i in 1 until contacts.size) {
                    duplicatedContacts.add(contacts[i])
                }
            }
        }

        callback.onSuccess(ContactsList(duplicatedContacts))
    }

    override fun onBind(intent: Intent): IBinder {
        return object : RemoveDuplicateContacts.Stub() {
            override fun execute(contactsList: ContactsList, callback: ResultCallback) {
                try {
                    removeDuplicateContacts(contactsList.contacts, callback)
                } catch (err: Exception) {
                    Log.e(TAG, "Error removing duplicates", err)
                    callback.onError(AidlException(
                        err.message,
                        AidlException.ERROR_REMOVING_DUPLICATION_EXCEPTION
                    ))
                }
            }
        }
    }

    companion object {
        private const val TAG = "RemoveDuplicateContacts"
    }
}
