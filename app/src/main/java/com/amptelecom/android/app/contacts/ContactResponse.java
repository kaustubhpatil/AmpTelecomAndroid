package com.amptelecom.android.app.contacts;

import com.amptelecom.android.app.network.model.Contacts;
import java.util.List;

public class ContactResponse {

    public List<Contacts> contacts;

    public List<Contacts> getContactEntries() {
        return contacts;
    }

    public void setContactEntries(List<Contacts> contactEntries) {
        this.contacts = contactEntries;
    }
}
