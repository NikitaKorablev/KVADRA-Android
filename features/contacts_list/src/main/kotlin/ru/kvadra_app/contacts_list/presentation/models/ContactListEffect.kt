package ru.kvadra_app.contacts_list.presentation.models

sealed class ContactListEffect {
    data class ShowToast(val message: String) : ContactListEffect()
    object ShowRationaleDialog : ContactListEffect()
}
