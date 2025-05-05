package com.kvadra.server.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.IBinder
import com.aidl.RemoveDuplicateContacts
import com.aidl.ResultCallback
import com.aidl.AidlException
import com.aidl.ContactsList
import com.kvadra_app.core.data.Contact

class DuplicateContactsRemoverService : Service() {
    private lateinit var resultReceiver: BroadcastReceiver

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(resultReceiver)
    }

    private fun mapListToMap(contacts: List<Contact>): Map<String, MutableList<Contact>> {
        val contactsMap = mutableMapOf<String, MutableList<Contact>>()

        for (contact in contacts) {
            val key = "${contact.name}-${contact.phoneNumber}"
            if (contactsMap.containsKey(key)) {
                contactsMap[key]?.add(contact)
            } else {
                contactsMap[key] = mutableListOf(contact)
            }
        }

        return contactsMap
    }

    private fun removeDuplicateContacts(contactsList: List<Contact>, callback: ResultCallback) {
        val contactsMap = mapListToMap(contactsList)

        val duplicatedContacts = mutableListOf<Contact>()
        for ((key, contacts) in contactsMap) {
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
