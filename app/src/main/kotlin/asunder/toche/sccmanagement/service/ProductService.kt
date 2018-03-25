package asunder.toche.sccmanagement.service

import android.content.Context
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper
import java.util.HashMap

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class ProductService(var listener:ProductCallback){


    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName
    private val context = FirebaseApp.getInstance()?.applicationContext
    interface ProductCallback{
        fun onSuccess()
        fun onFail()
    }


    fun pushNewProduct(product: Model.Product){
        val keyAuth = firebase.child("${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.PRODUCTS}").push().key
        product.id = keyAuth
        Log.d(TAG,"PushNewProduct with $product")
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.PRODUCTS}/$keyAuth"] = product
        firebase.updateChildren(childUpdates,{databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
                listener.onFail()
            } else {
                System.out.println("Data Product,Managemnt saved successfully.")
                listener.onSuccess()
            }
        })
        pushNewProductToDb(updateProductFromDb(product,getProductsInDb()))


    }

    fun updateProduct(product: Model.Product){
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.PRODUCTS}/${product.id}"] = product
        firebase.updateChildren(childUpdates,{ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
                listener.onFail()
            } else {
                System.out.println("Data Update Product successfully.")
                listener.onSuccess()
            }
        })
        pushNewProductToDb(updateProductFromDb(product,getProductsInDb()))
    }

    fun deleteProduct(product : Model.Product){
        firebase.child("${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.PRODUCTS}/${product.id}")
                .removeValue({ databaseError, _ ->
                    if (databaseError != null) {
                        //Crashlytics.log(databaseError.message)
                        listener.onFail()
                        System.out.println("Data could not be saved " + databaseError.message)
                    } else {
                        listener.onSuccess()
                        System.out.println("Data deleted successfully.")
                    }
                })
    }

    fun getProductsInDb() : MutableList<Model.Product>{
        val Product = Paper.book().read<Model.ProductUser>(ROOT.PRODUCTS)
        return if(Product != null) {
            Product.products
        }else{
            Log.d(TAG,"Not Found Product in DB")
            mutableListOf()
        }
    }

    fun pushNewProductToDb(Products:MutableList<Model.Product>) {
        Paper.book().write(ROOT.PRODUCTS,Model.ProductUser(Products))
    }

    fun deleteProductInDb(uid:String){
        val currentProduct = getProductsInDb()
        currentProduct
                .filter { it.id == uid }
                .forEach {
                    currentProduct.remove(it)
                }

        pushNewProductToDb(currentProduct)
    }

    fun updateProductFromDb(Product:Model.Product,ProductFromDb: MutableList<Model.Product>)
            : MutableList<Model.Product>{
        ProductFromDb.filter { it.id == Product.id }
                .forEach {
                    ProductFromDb.remove(it)
                }
        ProductFromDb.add(Product)
        return ProductFromDb
    }



}