package com.kvadra_app

import android.app.Application
import com.kvadra_app.contacts_list.di.ContactsListComponent
import com.kvadra_app.contacts_list.di.ContactsListDepsProvider
import com.kvadra_app.contacts_list.di.DaggerContactsListComponent
import com.kvadra_app.di.AppComponent
import com.kvadra_app.di.DaggerAppComponent

class KVADRAApplication: Application(), ContactsListDepsProvider {
    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }

    override fun getContactsListComponent(): ContactsListComponent {
        return DaggerContactsListComponent.builder().build()
    }
}