package asunder.toche.sccmanagement.custom;

/**
 * Created by ToCHe on 12/3/2018 AD.
 */

public class TriggerUpdate {

    private Boolean isUpdate;

    public TriggerUpdate(Boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public Boolean getUpdate() {
        return isUpdate;
    }

    public void setUpdate(Boolean update) {
        isUpdate = update;
    }
}
