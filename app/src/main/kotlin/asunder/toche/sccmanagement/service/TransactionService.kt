package asunder.toche.sccmanagement.service

import android.content.Context
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import com.crashlytics.android.Crashlytics
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.HashMap

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class TransactionService(var listener:TransactionCallback){


    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName
    private val context = FirebaseApp.getInstance()?.applicationContext
    interface TransactionCallback{
        fun onSuccess()
        fun onFail()
    }


    fun pushNewTransaction(transaction: Model.Transaction){
        val keyAuth = firebase.child("${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.TRANSACTIONS}").push().key
        transaction.id = keyAuth!!
        Log.d(TAG,"PushNewTransaction with Transaction")
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.TRANSACTIONS}/$keyAuth"] = transaction
        firebase.updateChildren(childUpdates,{databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Transaction,Managemnt saved successfully.")
            }
        })
        pushNewTransactionToDb(updateTransactionFromDb(transaction,getTransactionInDb()))
    }

    fun updateTransaction(transaction: Model.Transaction){
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.TRANSACTIONS}/${transaction.id}"] = transaction
        firebase.updateChildren(childUpdates,{ databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Update Transaction successfully.")
            }
        })
        pushNewTransactionToDb(updateTransactionFromDb(transaction,getTransactionInDb()))
    }

    fun deleteTransaction(transaction : Model.Transaction){
        firebase.child("${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.TRANSACTIONS}/${transaction.id}")
                .removeValue({ databaseError, _ ->
                    if (databaseError != null) {
                        Crashlytics.log(databaseError.message)
                        System.out.println("Data could not be saved " + databaseError.message)
                        listener.onFail()
                    } else {
                        System.out.println("Data deleted successfully.")
                        listener.onSuccess()
                    }
                })
    }


    fun getTransactionInDb() : MutableList<Model.Transaction>{
        val transaction = Paper.book().read<Model.TransactionUser>(ROOT.TRANSACTIONS)
        return if(transaction != null) {
            transaction.transactions
        }else{
            Log.d(TAG,"Not Found Transaction in DB")
            mutableListOf()
        }
    }

    fun pushNewTransactionToDb(transactions:MutableList<Model.Transaction>)  = async(UI) {
        try {
            val addTransaction = async {
                Paper.book().write(ROOT.TRANSACTIONS,Model.TransactionUser(transactions))
            }
            addTransaction.await()
            listener.onSuccess()
        }
        catch (e: Exception) {
        }
        finally {
        }
    }

    fun deleteTransactionInDb(uid:String){
        val currentTransaction = getTransactionInDb()
        currentTransaction
                .filter { it.id == uid }
                .forEach {
                    currentTransaction.remove(it)
                }

        pushNewTransactionToDb(currentTransaction)
    }

    fun updateTransactionFromDb(Transaction:Model.Transaction,TransactionFromDb: MutableList<Model.Transaction>)
            : MutableList<Model.Transaction>{
        TransactionFromDb.filter { it.id == Transaction.id }
                .forEach {
                    TransactionFromDb.remove(it)
                }
        TransactionFromDb.add(Transaction)
        return TransactionFromDb
    }





}