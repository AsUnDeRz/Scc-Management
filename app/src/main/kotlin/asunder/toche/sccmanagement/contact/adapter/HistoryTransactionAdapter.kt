package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import asunder.toche.sccmanagement.R

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class HistoryTransactionAdapter : RecyclerView.Adapter<HistoryTransactionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): HistoryTransactionHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_history_transaction
                ,parent,false)
        return HistoryTransactionHolder(view)
    }

    override fun getItemCount(): Int {
        return  10
    }

    override fun onBindViewHolder(holder: HistoryTransactionHolder?, position: Int) {
        holder?.bind()
    }


}