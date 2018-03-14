package asunder.toche.sccmanagement.service

import android.content.Context
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class ProductService{


    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName


    fun pushNewProduct(product: Model.Product, context: Context){
        val keyAuth = firebase.child("${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.PRODUCTS}").push().key
        product.id = keyAuth
        Log.d(TAG,"PushNewProduct with $product")
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.PRODUCTS}/$keyAuth"] = product
        firebase.updateChildren(childUpdates,{databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Product,Managemnt saved successfully.")
            }
        })

    }

    fun updateProduct(product: Model.Product, context: Context){
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.PRODUCTS}/${product.id}"] = product
        firebase.updateChildren(childUpdates,{ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Update Product successfully.")
            }
        })

    }

    fun deleteProduct(product : Model.Product, context: Context){
        firebase.child("${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.PRODUCTS}/${product.id}")
                .removeValue({ databaseError, _ ->
                    if (databaseError != null) {
                        //Crashlytics.log(databaseError.message)
                        System.out.println("Data could not be saved " + databaseError.message)
                    } else {
                        System.out.println("Data deleted successfully.")
                    }
                })
    }


}