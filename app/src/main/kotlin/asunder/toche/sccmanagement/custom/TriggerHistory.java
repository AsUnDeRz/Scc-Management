package asunder.toche.sccmanagement.custom;

import asunder.toche.sccmanagement.Model;

/**
 * Created by ToCHe on 17/3/2018 AD.
 */

public class TriggerHistory {

    public TriggerHistory(Model.Contact contact) {
        this.contact = contact;
    }

    private Model.Contact contact;

    public Model.Contact getContact() {
        return contact;
    }

    public void setContact(Model.Contact contact) {
        this.contact = contact;
    }
}
