package asunder.toche.sccmanagement.products

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.service.FirebaseManager
import asunder.toche.sccmanagement.service.ProductService
import java.io.File

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductViewModel : ViewModel(),ProductService.ProductCallback {


    val service = ProductService(this)
    val firebase = FirebaseManager()
    val products : MutableLiveData<MutableList<Model.Product>> = MutableLiveData()
    val stateView : MutableLiveData<ProductState> = MutableLiveData()
    var productId  =""



    fun saveProduct(data:Model.Product){
        if(productId == "") {
            service.pushNewProduct(data)
        }else{
            service.updateProduct(data)
        }
    }




    fun loadProduct(){
        val data = service.getProductsInDb()
        updateProducts(data)
    }


    fun updateStateView(state: ProductState){
        stateView.value = state
    }

    fun updateProducts(data : MutableList<Model.Product>){
        products.value = data
    }

    override fun onSuccess() {
    }

    override fun onFail() {
    }



}