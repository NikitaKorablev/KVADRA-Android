package com.kvadra_app.contacts_list.data

data class Contact(
    val name: String,
    val phoneNumber: String
)

data class ContactItem(
    val contact: Contact?,
    val header: Char?
)