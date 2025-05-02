package com.kvadra_app.contacts_list.di

import com.kvadra_app.contacts_list.presentation.ContactListActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component
interface ContactsListComponent {
    fun inject(contactsActivity: ContactListActivity)

    @Component.Builder
    interface Builder {
//        fun deps(deps: )
        fun build(): ContactsListComponent
    }
}

//interface ContactsListDeps {
//    val router: Router
//}

interface ContactsListDepsProvider {
    fun getContactsListComponent(): ContactsListComponent
}