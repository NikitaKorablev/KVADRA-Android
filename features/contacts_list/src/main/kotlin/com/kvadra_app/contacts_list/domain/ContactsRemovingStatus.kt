package com.kvadra_app.contacts_list.domain

sealed class ContactsRemovingStatus {
    data class Success(val message: String): ContactsRemovingStatus()
    data class Failed(val message: String, val err: String): ContactsRemovingStatus()

    companion object {
        const val NO_DUPLICATE_CONTACTS = "Повторяющиеся контакты удалены."
        const val SUCCESS_MESSAGE = "Повторяющиеся контакты удалены."
        const val UNEXPECTED_EXCEPTION = "Непредвиденная ошибка при удалении повторяющихся контактов."
        const val REMOVING_EXCEPTION = "Ошибка удаления повторяющихся контактов"
    }
}