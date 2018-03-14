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
class TransactionService{


    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName


    fun pushNewTransaction(transaction: Model.Transaction, context: Context){
        val keyAuth = firebase.child("${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.TRANSACTIONS}").push().key
        transaction.id = keyAuth
        Log.d(TAG,"PushNewTransaction with tTransaction")
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.TRANSACTIONS}/$keyAuth"] = transaction
        firebase.updateChildren(childUpdates,{databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Transaction,Managemnt saved successfully.")
            }
        })

    }

    fun updateTransaction(transaction: Model.Transaction, context: Context){
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.TRANSACTIONS}/${transaction.id}"] = transaction
        firebase.updateChildren(childUpdates,{ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Update Transaction successfully.")
            }
        })

    }

    fun deleteTransaction(transaction : Model.Transaction, context: Context){
        firebase.child("${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.TRANSACTIONS}/${transaction.id}")
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