package asunder.toche.sccmanagement.transactions.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.issue.adapter.SectionIssueAdapter
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ContactService
import asunder.toche.sccmanagement.service.FirebaseManager
import asunder.toche.sccmanagement.service.ProductService
import asunder.toche.sccmanagement.service.TransactionService
import asunder.toche.sccmanagement.transactions.TransactionListener
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.adapter.SectionTransactionAdapter
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

/**
 *Created by ToCHe on 20/3/2018 AD.
 */
class TransactionViewModel : ViewModel(),
        TransactionService.TransactionCallback,
        ContactService.ContactCallBack,
        ProductService.ProductCallback{

    val TAG = this::class.java.simpleName
    val service = TransactionService(this)
    val contactService = ContactService(this)
    val productService = ProductService(this)
    val firebase = FirebaseManager()
    val transaction: MutableLiveData<Model.Transaction> = MutableLiveData()
    val transactions : MutableLiveData<MutableList<Model.Transaction>> = MutableLiveData()
    val salePrice:MutableLiveData<Model.SalePrice> = MutableLiveData()
    val salePriceLists : MutableLiveData<MutableList<Model.SalePrice>> = MutableLiveData()
    val stateView : MutableLiveData<TransactionState> = MutableLiveData()
    val outState : MutableLiveData<TransactionState> = MutableLiveData()
    val product: MutableLiveData<Model.Product> = MutableLiveData()
    val contact: MutableLiveData<Model.Contact> = MutableLiveData()
    var transactionId  =""
    var salePricePosition:Int = 0




    fun getContact() : MutableList<Model.Contact>{
        return contactService.getContactInDb()
    }


    fun saveTransaction(data: Model.Transaction){
        if (data.sale_price.isNotEmpty()){
            data.date = data.sale_price.first().date
        }

        if (transactionId == ""){
            service.pushNewTransaction(data)
        }else{
            service.updateTransaction(data)
        }
    }

    fun deleteTransaction(){
        transaction.value?.let {
            service.deleteTransactionInDb(it.id)
            service.deleteTransaction(it)
        }
    }



    fun tranformFormat():Model.MasterGroup{
        val groupDate : MutableList<String> = mutableListOf()
        val groupCompany : MutableList<String> = mutableListOf()
        transactions.value?.forEach {
            groupDate.add(it.date.substring(0,10))
            groupCompany.add(it.company_id)
        }

        return Model.MasterGroup(groupDate.distinctBy { it }.sortedByDescending { Utils.getDateString(it).time  }.toMutableList()
                ,groupCompany.distinctBy { it }.sortedByDescending { it }.toMutableList())
    }

    fun separateSection(sectionList: List<String>, companyList:List<String>,
                        listener: TransactionListener) :
    MutableMap<String,SectionedRecyclerViewAdapter>{
        val data = transactions.value
        val products = productService.getProductsInDb()
        val companys = contactService.getContactInDb()
        val mapSection : MutableMap<String,SectionedRecyclerViewAdapter> = mutableMapOf()
        sectionList.forEach { sectionDate ->
            val sectionAdapter = SectionedRecyclerViewAdapter()
            Log.d(TAG,"SectionList [$sectionDate]")
            companyList.forEach {compId ->
                Log.d(TAG,"CompanyList [$compId]")
                val mapProduct : MutableMap<String,Model.Product> = mutableMapOf()
                val transaction = data?.filter { it.company_id == compId && sectionDate == it.date.substring(0,10)}
                if (transaction != null) {
                    if (transaction.isNotEmpty()) {
                        transaction.forEach { transacModel ->
                            Log.d(TAG,"Transaction [${transacModel.id}]")
                            val item = products.filter { it.id == transacModel.product_id }
                            if(item.isNotEmpty()) {
                                Log.d(TAG,"Map product with transaction [${item.first().product_name}]")
                                mapProduct[transacModel.id] = item.first()
                            }
                        }
                        val compName = companys.first { it.id == compId }
                        Log.d(TAG, "Transaction isNotEmpty [${compName.company}]")
                        sectionAdapter.addSection(
                                SectionTransactionAdapter(compName.company,
                                        transaction.toMutableList(),
                                        mapProduct,
                                        listener))
                        mapSection[sectionDate] = sectionAdapter
                    }
                }
            }
            //mapSection[sectionDate] = sectionAdapter
        }

        return mapSection
    }

    fun sortToday(listener: TransactionListener) : Model.MasterGroup{
        val masterGroup = tranformFormat()
        val sectionList = masterGroup.groupDate.filter { Utils.getDateString(it).time > Utils.getDateWithNumberFromCurrent(-1).time
                && Utils.getDateString(it).time < Utils.getCurrentDate().time }
        val companyList = masterGroup.groupCompany
        val result = separateSection(sectionList,companyList,listener)
        return if(result.isNotEmpty()) {
            masterGroup.resultMap = result
            masterGroup.groupDate = sectionList.toMutableList()
            masterGroup
        }else{
            Model.MasterGroup()
        }
    }

    fun sortTomorrow(listener: TransactionListener):Model.MasterGroup{
        val masterGroup = tranformFormat()
        val companyList = masterGroup.groupCompany
        val newDate = masterGroup.groupDate.filter { Utils.getDateString(it).time > Utils.getCurrentDate().time
                && Utils.getDateString(it).time <= Utils.getDateWithNumberFromCurrent(1).time }
        val result = separateSection(newDate,companyList,listener)
        return if(result.isNotEmpty()) {
            masterGroup.resultMap = result
            masterGroup.groupDate = newDate.toMutableList()
            masterGroup
        }else{
            Model.MasterGroup()
        }
    }


    fun sortYesterday(listener: TransactionListener):Model.MasterGroup{
        val masterGroup = tranformFormat()
        val companyList = masterGroup.groupCompany
        val newDate = masterGroup.groupDate.filter { Utils.getDateString(it).time < Utils.getPreviusDate().time
                && Utils.getDateString(it).time >= Utils.getDateWithNumberFromCurrent(-2).time }
        val result = separateSection(newDate
                ,companyList,listener)
        return if(result.isNotEmpty()) {
            masterGroup.resultMap = result
            masterGroup.groupDate = newDate.toMutableList()
            masterGroup
        }else{
            Model.MasterGroup()
        }
    }

    fun loadTransaction(){
        val data = service.getTransactionInDb()
        updateTransactions(data)
    }

    fun updateSalePrice(data:Model.SalePrice,position:Int){
        salePrice.value = data
        salePricePosition = position
    }

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
    }

    fun updateProduct(data: Model.Product?){
        product.value = data
    }

    fun updateContact(data:Model.Contact){
        contact.value = data
    }

    fun updateOutState(data:TransactionState){
        outState.value = data
    }

    override fun onSuccess() {
        updateStateView(TransactionState.SHOWLIST)
        loadTransaction()
    }

    override fun onFail() {
        updateStateView(TransactionState.SHOWLIST)
        loadTransaction()
    }

    override fun onDeleteSuccess() {

    }


}