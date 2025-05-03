package com.kvadra_app.contacts_list.domain

import com.kvadra_app.contacts_list.data.Contact

interface OnContactClickListener {
    fun onContactClick(contact: Contact)
}