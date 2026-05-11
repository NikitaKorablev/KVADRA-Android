// RemoveDuplicateContacts.aidl
package ru.kvadra_app.aidl;

import ru.kvadra_app.aidl.ResultCallback;
import ru.kvadra_app.aidl.ContactsList;

interface RemoveDuplicateContacts {
    void execute(in ContactsList contacts, ResultCallback callback);
}