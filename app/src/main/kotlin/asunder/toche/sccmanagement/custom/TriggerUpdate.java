package asunder.toche.sccmanagement.custom;

/**
 * Created by ToCHe on 12/3/2018 AD.
 */

public class TriggerUpdate {

    private Boolean isUpdate;

    private Object object;

    public TriggerUpdate(Boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public Boolean getUpdate() {
        return isUpdate;
    }

    public void setUpdate(Boolean update) {
        isUpdate = update;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
