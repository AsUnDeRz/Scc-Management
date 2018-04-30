package asunder.toche.sccmanagement.products.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.products.ProductState
import asunder.toche.sccmanagement.service.FirebaseManager
import asunder.toche.sccmanagement.service.ProductService

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductViewModel : ViewModel(),ProductService.ProductCallback {


    val service = ProductService(this)
    val firebase = FirebaseManager()
    val product : MutableLiveData<Model.Product> = MutableLiveData()
    val products : MutableLiveData<MutableList<Model.Product>> = MutableLiveData()
    val mediumRate : MutableLiveData<Model.MediumRate> = MutableLiveData()
    val mediumRateLists : MutableLiveData<MutableList<Model.MediumRate>> = MutableLiveData()
    val stateView : MutableLiveData<ProductState> = MutableLiveData()
    var productId  =""
    var mediumPosition:Int = 0



    fun saveProduct(data:Model.Product){
        System.out.println("SaveProduct  $data")
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

    fun updateMediumRateList(data : MutableList<Model.MediumRate>){
        mediumRateLists.value = data
    }

    fun updateMediumRate(data:Model.MediumRate,position:Int){
        mediumRate.value = data
        mediumPosition = position
    }

    fun updateProduct(product: Model.Product){
        this.product.value = product
    }

    fun addMediumRate(mediumRate: Model.MediumRate){
        mediumRateLists.value?.add(mediumRate)
    }

    fun deleteProduct(product: Model.Product){
        service.deleteProductInDb(product.id)
        service.deleteProduct(product)
    }

    override fun onSuccess() {
        updateStateView(ProductState.SHOWLIST)
        loadProduct()
    }

    override fun onFail() {
        updateStateView(ProductState.SHOWLIST)
        loadProduct()
    }



}