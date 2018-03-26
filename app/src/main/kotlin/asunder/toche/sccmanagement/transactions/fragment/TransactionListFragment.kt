package asunder.toche.sccmanagement.transactions.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.transactions.TransactionListener
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.adapter.MasterSectionAdapter
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import kotlinx.android.synthetic.main.fragment_transactions_list.*

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class TransactionListFragment : Fragment(),TransactionListener {

    companion object {
        fun newInstance(): TransactionListFragment = TransactionListFragment()
    }

    lateinit var transactionVM : TransactionViewModel
    lateinit var masterSectionAdapter: MasterSectionAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionVM = ViewModelProviders.of(activity!!).get(TransactionViewModel::class.java)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_transactions_list,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAdapter()
        observeTransactions()
        transactionVM.loadTransaction()
    }

    fun setUpAdapter(){
        masterSectionAdapter = MasterSectionAdapter(Model.MasterGroup())
        rvTransaction.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter =masterSectionAdapter
        }
    }

    fun observeTransactions(){
        transactionVM.transactions.observe(this, Observer {
            masterSectionAdapter.updateMasterGroup(transactionVM.sortAll(this))

        })
        transactionVM.stateView.observe(this, Observer {
            when(it){
                TransactionState.SORTALL ->{
                    masterSectionAdapter.updateMasterGroup(transactionVM.sortAll(this))
                }
                TransactionState.SORTYESTERDAY ->{
                    masterSectionAdapter.updateMasterGroup(transactionVM.sortYesterday(this))
                }
                TransactionState.SORTTOMORROW ->{
                    masterSectionAdapter.updateMasterGroup(transactionVM.sortTomorrow(this))
                }
            }
        })
    }

    override fun onClickTransaction(transaction: Model.Transaction) {
        transactionVM.updateStateView(TransactionState.SHOWTRANSACTION)
        transactionVM.updateTransaction(transaction)

    }

}