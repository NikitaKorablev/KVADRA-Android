package com.kvadra_app.contacts_list.domain

sealed class ContactsRemovingStatus {
    data class Success(val message: String): ContactsRemovingStatus()
    data class Failed(val message: String, val err: String): ContactsRemovingStatus()
}