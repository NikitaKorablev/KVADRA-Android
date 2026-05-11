package com.kvadra_app.contacts_list.presentation

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kvadra_app.contacts_list.R
import ru.kvadra_app.model.Contact
import com.kvadra_app.contacts_list.databinding.FragmentContactListBinding
import ru.kvadra_app.contacts_list.domain.OnContactClickListener
import androidx.core.net.toUri
import ru.kvadra_app.aidl.AidlException
import ru.kvadra_app.aidl.ContactsList
import ru.kvadra_app.aidl.RemoveDuplicateContacts
import ru.kvadra_app.aidl.ResultCallback
import ru.kvadra_app.contacts_list.presentation.adapters.ContactsAdapter
import ru.kvadra_app.contacts_list.utils.ContactsManager
import ru.kvadra_app.contacts_list.utils.ContactsPermissionManager
import ru.kvadra_app.model.ResultState

class ContactListFragment : Fragment(), OnContactClickListener {
    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!
    
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsManager = ContactsManager(context = requireContext())
        permissionManager = ContactsPermissionManager(
            onPermissionGranted = { loadContacts() },
            onShowRationaleDialog = { showRationaleDialog() },
            onShowSettingsDialog = { showSettingsDialog() },
            context = requireContext()
        )
        permissionManager.initialize(this)
        permissionManager.checkAndRequestContactsPermission()

        binding.serviceButton.setOnClickListener(this::onServiceButtonClick)
    }

    override fun onStart() {
        super.onStart()
        val intent = createExplicitIntent()
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unbindService(serviceConnection)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun onServiceButtonClick(view: View?) {
        if (permissionManager.hasContactsPermission()) {

            removeDuplicateContacts?.execute(contactsList, object : ResultCallback.Stub() {
                override fun onSuccess(contacts: ContactsList) {
                    activity?.runOnUiThread {
                        when (val res = contactsManager.deleteContacts(contacts.contacts)) {
                            is ResultState.Error -> showToast(res.error)

                            is ResultState.Success -> {
                                showToast(res.data)
                                loadContacts()
                            }
                        }
                    }
                }

                override fun onError(aidlException: AidlException) {
                    activity?.runOnUiThread {
                        showToast(getString(R.string.removing_exception))
                        Log.e(TAG, aidlException.toException().message.toString())
                    }
                }
            })

        } else permissionManager.checkAndRequestContactsPermission()
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(requireContext())
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
        AlertDialog.Builder(requireContext())
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
        val contacts = contactsManager.getContacts()
        contactsList = ContactsList(contacts)

        if (contacts.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyTextView.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyTextView.visibility = View.GONE

            val contactItems = contactsManager.groupContactsByLetter(contacts)
            val adapter = binding.recyclerView.adapter as? ContactsAdapter
            if (adapter != null) {
                adapter.updateContacts(contactItems)
            } else {
                binding.recyclerView.adapter = ContactsAdapter(contactItems, this)
            }
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
        return Intent("ru.kvadra_app.server.service.DuplicateContactsRemoverService").apply {
            `package` = requireContext().packageName
        }
    }

    companion object {
        private const val TAG = "ContactListFragment"
    }
}
