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
import asunder.toche.sccmanagement.contact.adapter.HistoryIssueAdapter
import asunder.toche.sccmanagement.contact.adapter.HistoryTransactionAdapter
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.TriggerHistory
import asunder.toche.sccmanagement.service.IssueService
import kotlinx.android.synthetic.main.fragment_contact_history.*
import kotlinx.android.synthetic.main.layout_history_issue.*
import kotlinx.android.synthetic.main.layout_history_transaction.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class HistoryCompanyFragment : Fragment() {



    lateinit var contactVM : ContactViewModel
    lateinit var historyIssueAdapter: HistoryIssueAdapter
    lateinit var historyTransactionAdapter: HistoryTransactionAdapter

    companion object {
        fun newInstance(): HistoryCompanyFragment = HistoryCompanyFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactVM = ViewModelProviders.of(this).get(ContactViewModel::class.java)
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
            Snackbar.make(root_history_company,"Add Issue",Snackbar.LENGTH_SHORT).show()
        }

        btnAddTransactionWithCompany.setOnClickListener {
            Snackbar.make(root_history_company,"Add Transaction",Snackbar.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public fun onTriggerHistory(trigger : TriggerHistory) {
        val stickyEvent = EventBus.getDefault().getStickyEvent(TriggerHistory::class.java)
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent)
        }
        updateIssueAdapter(trigger.contact)
    }

}