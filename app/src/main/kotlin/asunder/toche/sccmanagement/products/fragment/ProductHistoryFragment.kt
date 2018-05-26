package asunder.toche.sccmanagement.products.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.TriggerProduct
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.adapter.ProductHistoryAdapter
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import kotlinx.android.synthetic.main.fragment_product_history.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductHistoryFragment : Fragment(),
ProductHistoryAdapter.ProductHistoryOnClickListener{

    companion object {
        fun newInstance(): ProductHistoryFragment = ProductHistoryFragment()
    }

    private lateinit var transactionVM : TransactionViewModel
    private lateinit var productHistoryAdapter: ProductHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionVM = ViewModelProviders.of(activity!!).get(TransactionViewModel::class.java)
        EventBus.getDefault().register(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_product_history,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
    }



    fun setupAdapter(){
        productHistoryAdapter = ProductHistoryAdapter(this)
        rvCompanyPrefer.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = productHistoryAdapter
        }
    }

    fun updateProductAdapter(product: Model.Product?){
        System.out.println("History product   $product")
        Utils.findTransaction(product!!.id,object : Utils.OnFindTransactionsListener{
            override fun onResults(results: MutableList<Model.Transaction>) {
                val mapTransaction:MutableMap<Model.Transaction,Model.Contact> = mutableMapOf()
                val contacts = transactionVM.getContact()
                val groupCompany = mutableListOf<String>()
                val sortTranstions = mutableListOf<Model.Transaction>()
                async(UI){
                    val result = async {
                        results.sortByDescending { Utils.getDateWithString(it.date).time }
                    }
                    result.await()
                }
                results.mapTo(groupCompany,{
                    it.company_id
                })
                groupCompany.forEach { company ->
                    val data = results.filter { company == it.company_id  }
                            .sortedByDescending { Utils.getDateWithString(it.date).time }
                            .first()
                    sortTranstions.add(data)
                    val comp = contacts.first { company == it.id  }
                    mapTransaction[data] = comp
                }
                async(UI) {
                    val def = async {
                        sortTranstions.distinctBy { it.company_id }
                    }
                    productHistoryAdapter.updateTransaction(mapTransaction,def.await().toMutableList())
                }
            }
        },transactionVM.service.getTransactionInDb(), ROOT.PRODUCTS)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public fun onTriggerProduct(trigger : TriggerProduct) {
        val stickyEvent = EventBus.getDefault().getStickyEvent(TriggerProduct::class.java)
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent)
        }
        updateProductAdapter(trigger.product)
    }

    override fun OnClickHistoryProduct(transaction: Model.Transaction) {
        transactionVM.updateStateView(TransactionState.SHOWTRANSACTION)
        transactionVM.updateTransaction(transaction)
    }

}