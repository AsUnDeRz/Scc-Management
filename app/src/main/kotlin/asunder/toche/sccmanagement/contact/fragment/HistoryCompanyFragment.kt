package asunder.toche.sccmanagement.contact.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.adapter.CompanyAdapter
import asunder.toche.sccmanagement.contact.adapter.HistoryIssueAdapter
import asunder.toche.sccmanagement.contact.adapter.HistoryTransactionAdapter
import kotlinx.android.synthetic.main.layout_history_issue.*
import kotlinx.android.synthetic.main.layout_history_transaction.*

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class HistoryCompanyFragment : Fragment() {



    companion object {
        fun newInstance(): HistoryCompanyFragment = HistoryCompanyFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            //adapter = HistoryTransactionAdapter()
        }
        rvHistoryIssue.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
           // adapter = HistoryIssueAdapter()
        }
    }
}