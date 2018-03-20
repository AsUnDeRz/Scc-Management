package asunder.toche.sccmanagement.custom;

import asunder.toche.sccmanagement.Model;

/**
 * Created by ToCHe on 20/3/2018 AD.
 */

public class TriggerProduct {

    public TriggerProduct(Model.Product product) {
        this.product = product;
    }

    private Model.Product product;


    public Model.Product getProduct() {
        return product;
    }

    public void setProduct(Model.Product product) {
        this.product = product;
    }
}
