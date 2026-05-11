// RemoveDuplicateContacts.aidl
package com.aidl;

import com.aidl.ResultCallback;
import com.aidl.ContactsList;

interface RemoveDuplicateContacts {
    void execute(in ContactsList contacts, ResultCallback callback);
}