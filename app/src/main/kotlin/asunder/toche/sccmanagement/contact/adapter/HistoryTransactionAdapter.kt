package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.transactions.TransactionListener

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class HistoryTransactionAdapter(var listener : TransactionListener?) : RecyclerView.Adapter<HistoryTransactionHolder>() {

    var mapTransaction : MutableMap<Model.Transaction,Model.Product> = mutableMapOf()
    var transactions : MutableList<Model.Transaction> = mutableListOf()

    fun updateMapTransaction(map:MutableMap<Model.Transaction,Model.Product>,
                             newData:MutableList<Model.Transaction>,
                             newListener: TransactionListener){
        listener = newListener
        transactions = newData
        mapTransaction = map
        sortTransaction()
        notifyDataSetChanged()
    }
    fun updateMapTransaction(map:MutableMap<Model.Transaction,Model.Product>,
                             newData:MutableList<Model.Transaction>){
        transactions = newData
        mapTransaction = map
        sortTransaction()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryTransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_transaction
                ,parent,false)
        return HistoryTransactionHolder(view)
    }

    override fun getItemCount(): Int {
        return  transactions.size
    }

    override fun onBindViewHolder(holder: HistoryTransactionHolder, position: Int) {
            holder.bind(transactions[position], mapTransaction[transactions[position]]!!, listener)

    }

    fun sortTransaction(){
        transactions.sortBy { it.product_name }
    }

}