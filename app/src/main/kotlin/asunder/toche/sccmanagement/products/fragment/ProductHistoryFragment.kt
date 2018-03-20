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
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.adapter.ProductHistoryAdapter
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import kotlinx.android.synthetic.main.fragment_product_history.*
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
        val data = mutableListOf<Model.Transaction>()
        val price = mutableListOf<Model.SalePrice>()
        for (i in 0 until 3){
            price.add(Model.SalePrice("1$i",true,"50$i", Utils.getCurrentDateShort(),""))
        }
        for (i in 0 until 30){
            data.add(Model.Transaction("","","",""
                    ,Utils.getCurrentDateShort(),"",price))
        }
        productHistoryAdapter = ProductHistoryAdapter(data,this)
        rvCompanyPrefer.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = productHistoryAdapter
        }
    }

    fun updateProductAdapter(product: Model.Product?){
        System.out.println("History product   $product")
        /*
        val result = IssueService(object : IssueService.IssueCallBack{
            override fun onIssueSuccess() {
            }
            override fun onIssueFail() {
            }
        }).getIssueInDb()
        historyIssueAdapter = HistoryIssueAdapter(result.filter { it.company_id == contact?.id } as MutableList<Model.Issue>)
        rvHistoryIssue.adapter = historyIssueAdapter
        */

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