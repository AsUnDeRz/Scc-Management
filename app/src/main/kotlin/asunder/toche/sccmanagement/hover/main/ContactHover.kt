package asunder.toche.sccmanagement.hover.main

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.adapter.HistoryIssueAdapter
import asunder.toche.sccmanagement.contact.adapter.HistoryTransactionAdapter
import asunder.toche.sccmanagement.hover.HoverService
import asunder.toche.sccmanagement.main.ActivityMain
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ContactService
import asunder.toche.sccmanagement.service.IssueService
import asunder.toche.sccmanagement.service.ProductService
import asunder.toche.sccmanagement.service.TransactionService
import asunder.toche.sccmanagement.transactions.IssueListener
import asunder.toche.sccmanagement.transactions.TransactionListener
import io.mattcarroll.hover.Content
import kotlinx.android.synthetic.main.contact_hover.view.*
import kotlinx.android.synthetic.main.layout_history_issue.view.*
import kotlinx.android.synthetic.main.layout_history_transaction.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class ContactHover(context: Context,
                   private val sectionId:String) : FrameLayout(context), Content,
        ContactService.ContactCallBack,
        IssueService.IssueCallBack,
        ProductService.ProductCallback,
        TransactionService.TransactionCallback,
        TransactionListener,
        IssueListener{

    private var contactService :ContactService
    private var issueService : IssueService
    private var productService :ProductService
    private var transactionService : TransactionService
    lateinit var company:Model.Contact
    lateinit var historyIssueAdapter: HistoryIssueAdapter
    lateinit var historyTransactionAdapter: HistoryTransactionAdapter
    init {
        issueService = IssueService(this)
        productService = ProductService(this)
        transactionService = TransactionService(this)
        contactService = ContactService(this)
        initUI()
    }

    fun findCompany(){
        Utils.findCompany(sectionId,object : Utils.OnFindCompanyListener{
            override fun onResults(results: MutableList<Model.Contact>) {
                company = results.first()
                edtSearch.text = "คุณ ${company.contact_name.trim()} จาก ${company.company.trim()}"
                updateIssueAdapter(company)
                updateTransactionAdapter(company)
            }

        },contactService.getContactInDb())
    }

    fun initUI(){
        LayoutInflater.from(context).inflate(R.layout.contact_hover, this, true)
        imgClose.setOnClickListener {
            HoverService.deleteHover(sectionId)
        }

        imgClose.bringToFront()
        imgClose.invalidate()

        rvHistoryTransaction.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        rvHistoryIssue.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        findCompany()
    }



    fun updateIssueAdapter(contact: Model.Contact?){
        val result = issueService.getIssueInDb()
        historyIssueAdapter = HistoryIssueAdapter(mutableListOf())
        rvHistoryIssue.adapter = historyIssueAdapter
        async(UI) {
            val filterResult = async {
                result.filter { it.company_id == contact?.id }
            }
            historyIssueAdapter.updateIssues(
                    filterResult.await().toMutableList()
                    ,this@ContactHover)
        }
    }

    fun updateTransactionAdapter(contact: Model.Contact?){
        historyTransactionAdapter = HistoryTransactionAdapter()
        rvHistoryTransaction.adapter = historyTransactionAdapter
        Utils.findTransaction(contact?.id!!,object : Utils.OnFindTransactionsListener{
            override fun onResults(results: MutableList<Model.Transaction>) {
                val products = productService.getProductsInDb()
                val mapTransaction : MutableMap<Model.Transaction,Model.Product> = mutableMapOf()
                async(UI) {
                    val filterResult = async {
                        results.filter { it.company_id == contact.id }
                    }
                    filterResult.await().forEach { item ->
                        val product = products.first { it.id == item.product_id}
                        mapTransaction[item] = product
                    }
                    historyTransactionAdapter.updateMapTransaction(
                            mapTransaction
                            ,filterResult.await().toMutableList()
                            ,this@ContactHover)
                }
            }
        },transactionService.getTransactionInDb())

    }

    override fun onClickTransaction(transaction: Model.Transaction) {
        HoverService.mHoverView.collapse()
        val intent  = Intent()
        intent.putExtra(ROOT.CONTACTS,company)
        intent.putExtra(ROOT.TRANSACTIONS,transaction)
        context.startActivity(intent.setClass(context,ActivityMain::class.java))    }

    override fun onClickIssue(issue: Model.Issue) {
        HoverService.mHoverView.collapse()
        val intent  = Intent()
        intent.putExtra(ROOT.CONTACTS,company)
        intent.putExtra(ROOT.ISSUE,issue)
        context.startActivity(intent.setClass(context,ActivityMain::class.java))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }


    override fun getView(): View {
        return this
    }

    override fun isFullscreen(): Boolean {
        return true
    }

    override fun onShown() {
    }

    override fun onHidden() {
    }

    override fun onSuccess() {
    }

    override fun onFail() {
    }

    override fun onIssueSuccess() {
    }

    override fun onIssueFail() {
    }


}