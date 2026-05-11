package ru.kvadra_app.contacts_list.domain

import ru.kvadra_app.model.Contact

interface OnContactClickListener {
    fun onContactClick(contact: Contact)
}