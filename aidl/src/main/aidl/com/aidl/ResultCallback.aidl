// ResultCallback.aidl
package com.aidl;

import com.aidl.AidlException;
import com.aidl.ContactsList;

interface ResultCallback {
    void onSuccess(in ContactsList contacts);
    void onError(in AidlException aidlException);
}