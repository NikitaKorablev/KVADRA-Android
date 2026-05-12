package ru.kvadra_app.contacts_list.presentation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.kvadra_app.contacts_list.R
import ru.kvadra_app.contacts_list.domain.OnContactClickListener
import ru.kvadra_app.contacts_list.presentation.models.ContactListEffect
import ru.kvadra_app.contacts_list.presentation.theme.ContactsTheme
import ru.kvadra_app.contacts_list.utils.ContactsPermissionManager
import ru.kvadra_app.model.Contact

@AndroidEntryPoint
class ContactListFragment : Fragment(), OnContactClickListener {
    private lateinit var permissionManager: ContactsPermissionManager
    private val viewModel: ContactListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ContactsTheme {
                    ContactListScreen(
                        contactItems = viewModel.contactItemsState.value,
                        onContactClick = { contact -> onContactClick(contact) },
                        onServiceButtonClick = { 
                            viewModel.onServiceButtonClick(permissionManager.hasContactsPermission()) 
                        },
                        isEmpty = viewModel.isEmptyState.value
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionManager = ContactsPermissionManager(
            onPermissionGranted = { viewModel.loadContacts() },
            onShowRationaleDialog = { showRationaleDialog() },
            onShowSettingsDialog = { showSettingsDialog() },
            context = requireContext()
        )
        permissionManager.initialize(this)
        permissionManager.checkAndRequestContactsPermission()

        observeEffects()
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is ContactListEffect.ShowToast -> showToast(effect.message)
                        is ContactListEffect.ShowRationaleDialog -> showRationaleDialog()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.bindService()
    }

    override fun onStop() {
        super.onStop()
        viewModel.unbindService()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
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

    override fun onContactClick(contact: Contact) {
        Log.d(TAG, "Contact clicked: ${contact.phoneNumber}")
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = "tel:${contact.phoneNumber}".toUri()
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "ContactListFragment"
    }
}
