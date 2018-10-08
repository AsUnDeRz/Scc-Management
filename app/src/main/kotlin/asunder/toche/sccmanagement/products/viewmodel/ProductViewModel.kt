package asunder.toche.sccmanagement.products.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.products.ProductState
import asunder.toche.sccmanagement.service.FirebaseManager
import asunder.toche.sccmanagement.service.ProductService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.io.File

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

    fun saveProduct(data:Model.Product) = async(UI) {
        try {
            val job = async(CommonPool) {
                if(data.files.isNotEmpty()){
                    data.files.forEach {
                        val filePath = Uri.fromFile(File(it.local_path))
                        //it.cloud_url = "${ROOT.IMAGES}/${Prefer.getUUID(firebase.context!!)}/${filePath.lastPathSegment}"
                        firebase.pushFileToFirebase(it.local_path,"")
                        async(UI) {
                            val result = async {
                                //it.local_path = firebase.getPathClone(it.local_path)
                            }
                            result.await()
                        }
                    }
                }

                if(data.pictures.isNotEmpty()){
                    data.pictures.forEach {
                        val imgPath = Uri.fromFile(File(it.local_path))
                        it.cloud_url = "${ROOT.IMAGES}/${Prefer.getUUID(firebase.context!!)}/${imgPath.lastPathSegment}"
                        //firebase.pushFileToFirebase(it.local_path, "")
                        async(UI) {
                            val result = async {
                                //it.local_path = firebase.getPathClone(it.local_path)
                            }
                            result.await()
                        }
                    }
                }

            }
            job.await()
            System.out.println("SaveProduct  $data")
            if(productId == "") {
                service.pushNewProduct(data)
            }else{
                service.updateProduct(data)
            }

        }
        catch (e: Exception) {
        }
        finally {
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
        async(UI) {
            val result = async {
                product.files.forEach {
                    firebase.deleteFile(it.cloud_url,it.local_path)
                }
                product.pictures.forEach {
                    firebase.deleteFile(it.cloud_url,it.local_path)
                }
            }
            result.await()
            service.deleteProductInDb(product.id)
            service.deleteProduct(product)
        }
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