package asunder.toche.sccmanagement.contact.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.contact.adapter.HistoryIssueAdapter
import asunder.toche.sccmanagement.contact.adapter.HistoryTransactionAdapter
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.TriggerContact
import asunder.toche.sccmanagement.issue.IssueState
import asunder.toche.sccmanagement.issue.IssueViewModel
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.IssueService
import asunder.toche.sccmanagement.service.ProductService
import asunder.toche.sccmanagement.service.TransactionService
import asunder.toche.sccmanagement.transactions.TransactionListener
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import kotlinx.android.synthetic.main.fragment_contact_history.*
import kotlinx.android.synthetic.main.layout_history_issue.*
import kotlinx.android.synthetic.main.layout_history_transaction.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class HistoryCompanyFragment : Fragment(),TransactionListener {

    lateinit var contactVM : ContactViewModel
    lateinit var transactionVM : TransactionViewModel
    lateinit var issueVM : IssueViewModel
    lateinit var historyIssueAdapter: HistoryIssueAdapter
    lateinit var historyTransactionAdapter: HistoryTransactionAdapter

    companion object {
        fun newInstance(): HistoryCompanyFragment = HistoryCompanyFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactVM = ViewModelProviders.of(activity!!).get(ContactViewModel::class.java)
        transactionVM = ViewModelProviders.of(activity!!).get(TransactionViewModel::class.java)
        issueVM = ViewModelProviders.of(activity!!).get(IssueViewModel::class.java)
        EventBus.getDefault().register(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_contact_history, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvHistoryTransaction.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        rvHistoryIssue.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        initUI()
    }

    fun initUI(){
        btnAddIssueWithCompany.setOnClickListener {
            contactVM.updateViewState(ContactState.NEWISSUE)
        }

        btnAddTransactionWithCompany.setOnClickListener {
            transactionVM.updateContact(contactVM.contact.value!!)
            contactVM.updateViewState(ContactState.NEWTRANSACTION)
        }
    }



    fun updateIssueAdapter(contact: Model.Contact?){
            val result = IssueService(object : IssueService.IssueCallBack{
                override fun onIssueSuccess() {
                }
                override fun onIssueFail() {
                }
            }).getIssueInDb()
            historyIssueAdapter = HistoryIssueAdapter(result.filter { it.company_id == contact?.id } as MutableList<Model.Issue>)
            rvHistoryIssue.adapter = historyIssueAdapter
    }

    fun updateTransactionAdapter(contact: Model.Contact?){
        historyTransactionAdapter = HistoryTransactionAdapter()
        rvHistoryTransaction.adapter = historyTransactionAdapter
        Utils.findTransaction(contact?.id!!,object : Utils.OnFindTransactionsListener{
            override fun onResults(results: MutableList<Model.Transaction>) {
                val products = ProductService(object : ProductService.ProductCallback{
                    override fun onSuccess() {
                    }

                    override fun onFail() {
                    }
                }).getProductsInDb()
                val mapTransaction : MutableMap<Model.Transaction,Model.Product> = mutableMapOf()
                results.forEach {item ->
                    val product = products.first { it.id == item.product_id}
                    mapTransaction[item] = product
                }
                historyTransactionAdapter.updateMapTransaction(
                        mapTransaction
                        ,results
                        ,this@HistoryCompanyFragment)
            }
        },TransactionService(object : TransactionService.TransactionCallback{
            override fun onFail() {
            }

            override fun onSuccess() {
            }

        }).getTransactionInDb())

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public fun onTriggerContact(trigger : TriggerContact) {
        val stickyEvent = EventBus.getDefault().getStickyEvent(TriggerContact::class.java)
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent)
        }
        updateIssueAdapter(trigger.contact)
        updateTransactionAdapter(trigger.contact)
    }

    override fun onClickTransaction(transaction: Model.Transaction) {
    }

}