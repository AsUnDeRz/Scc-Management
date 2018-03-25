package asunder.toche.sccmanagement.transactions.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.transactions.TransactionListener

/**
 *Created by ToCHe on 24/3/2018 AD.
 */
class TransactionHistoryAdapter : RecyclerView.Adapter<TransactionHistoryAdapter.HistoryHolder>() {

    var mapTransaction : MutableMap<Model.Transaction,Model.Contact> = mutableMapOf()
    var transactions : MutableList<Model.Transaction> = mutableListOf()
    lateinit var listener: TransactionListener

    fun updateTransaction(newData : MutableMap<Model.Transaction,Model.Contact>,
                          newTransactions :MutableList<Model.Transaction>,
                          newListener : TransactionListener){
        mapTransaction = newData
        transactions = newTransactions
        listener = newListener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_history,parent,false)
        return HistoryHolder(view)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        holder.bind(transactions[position],mapTransaction[transactions[position]]!!,listener)
    }


    class HistoryHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val txtCompany = itemView?.findViewById<TxtMedium>(R.id.txtCompany)
        val txtPriceSale = itemView?.findViewById<TxtMedium>(R.id.txtPriceSale)
        val txtVat = itemView?.findViewById<TxtMedium>(R.id.txtVat)
        val txtValues = itemView?.findViewById<TxtMedium>(R.id.txtValues)
        val txtDate = itemView?.findViewById<TxtMedium>(R.id.txtDate)

        fun bind(transaction: Model.Transaction,contact:Model.Contact,listner:TransactionListener){
            txtCompany?.text = contact.company
            txtPriceSale?.text = transaction.sale_price[0].price
            if(transaction.sale_price[0].vat){
                txtVat?.text = "A"
            }else{
                txtVat?.text = "B"
            }
            txtValues?.text = transaction.sale_price[0].values
            txtDate?.text = transaction.date.substring(0,10)
            itemView.setOnClickListener {
                listner.onClickTransaction(transaction)
            }


        }

    }


}