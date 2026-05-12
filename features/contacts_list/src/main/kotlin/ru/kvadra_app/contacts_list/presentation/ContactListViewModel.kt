package ru.kvadra_app.contacts_list.presentation

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.kvadra_app.aidl.AidlException
import ru.kvadra_app.aidl.ContactsList
import ru.kvadra_app.aidl.RemoveDuplicateContacts
import ru.kvadra_app.aidl.ResultCallback
import ru.kvadra_app.contacts_list.presentation.models.ContactListEffect
import ru.kvadra_app.contacts_list.utils.ContactsManager
import ru.kvadra_app.model.ContactItem
import ru.kvadra_app.model.ResultState
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val contactsManager: ContactsManager,
    private val application: Application
) : ViewModel() {

    val contactItemsState = mutableStateOf<List<ContactItem>>(emptyList())
    val isEmptyState = mutableStateOf(true)

    private val _effect = MutableSharedFlow<ContactListEffect>()
    val effect = _effect.asSharedFlow()

    private var removeDuplicateContacts: RemoveDuplicateContacts? = null
    private var contactsList: ContactsList? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            removeDuplicateContacts = RemoveDuplicateContacts.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            removeDuplicateContacts = null
        }
    }

    fun bindService() {
        val intent = Intent("ru.kvadra_app.server.service.DuplicateContactsRemoverService").apply {
            `package` = application.packageName
        }
        application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        application.unbindService(serviceConnection)
    }

    fun loadContacts() {
        viewModelScope.launch {
            val contacts = contactsManager.getContacts()
            contactsList = ContactsList(contacts)

            if (contacts.isEmpty()) {
                isEmptyState.value = true
                contactItemsState.value = emptyList()
            } else {
                isEmptyState.value = false
                contactItemsState.value = contactsManager.groupContactsByLetter(contacts)
            }
        }
    }

    fun onServiceButtonClick(hasPermission: Boolean) {
        if (hasPermission) {
            val list = contactsList ?: return
            removeDuplicateContacts?.execute(list, object : ResultCallback.Stub() {
                override fun onSuccess(contacts: ContactsList) {
                    viewModelScope.launch {
                        when (val res = contactsManager.deleteContacts(contacts.contacts)) {
                            is ResultState.Error -> _effect.emit(ContactListEffect.ShowToast(res.error))
                            is ResultState.Success -> {
                                _effect.emit(ContactListEffect.ShowToast(res.data))
                                loadContacts()
                            }
                        }
                    }
                }

                override fun onError(aidlException: AidlException) {
                    viewModelScope.launch {
                        _effect.emit(ContactListEffect.ShowToast("Error removing contacts"))
                        Log.e(TAG, aidlException.toException().message.toString())
                    }
                }
            })
        } else viewModelScope.launch {
            _effect.emit(ContactListEffect.ShowRationaleDialog)
        }
    }

    companion object {
        private const val TAG = "ContactListViewModel"
    }
}
