package com.kvadra.server.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.IBinder
import android.util.Log
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
