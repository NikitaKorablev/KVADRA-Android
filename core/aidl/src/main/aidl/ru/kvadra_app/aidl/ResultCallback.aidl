// ResultCallback.aidl
package ru.kvadra_app.aidl;

import ru.kvadra_app.aidl.AidlException;
import ru.kvadra_app.aidl.ContactsList;

interface ResultCallback {
    void onSuccess(in ContactsList contacts);
    void onError(in AidlException aidlException);
}