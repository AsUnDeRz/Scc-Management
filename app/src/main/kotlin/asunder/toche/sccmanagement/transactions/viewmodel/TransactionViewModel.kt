package asunder.toche.sccmanagement.transactions.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.service.FirebaseManager
import asunder.toche.sccmanagement.service.TransactionService
import asunder.toche.sccmanagement.transactions.TransactionState

/**
 *Created by ToCHe on 20/3/2018 AD.
 */
class TransactionViewModel : ViewModel() {

    val service = TransactionService()
    val firebase = FirebaseManager()
    val transaction: MutableLiveData<Model.Transaction> = MutableLiveData()
    val transactions : MutableLiveData<MutableList<Model.Transaction>> = MutableLiveData()
    val salePriceLists : MutableLiveData<MutableList<Model.SalePrice>> = MutableLiveData()
    val stateView : MutableLiveData<TransactionState> = MutableLiveData()
    var transactionId  =""













    fun updateSalePriceLists(data : MutableList<Model.SalePrice>){
        salePriceLists.value = data
    }

    fun updateTransactions(data : MutableList<Model.Transaction>){
        transactions.value = data
    }

    fun updateTransaction(data: Model.Transaction){
        transaction.value = data
    }

    fun updateStateView(state:TransactionState){
        stateView.value = state
        System.out.println("UpdateStateView $state")
    }




}