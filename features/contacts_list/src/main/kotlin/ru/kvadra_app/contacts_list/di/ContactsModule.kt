package ru.kvadra_app.contacts_list.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.kvadra_app.contacts_list.utils.ContactsManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContactsModule {

    @Provides
    @Singleton
    fun provideContactsManager(
        @ApplicationContext context: Context
    ): ContactsManager = ContactsManager(context)
}
