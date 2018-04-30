package asunder.toche.sccmanagement.products.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.preference.ROOT

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductHistoryAdapter(var listener :
ProductHistoryOnClickListener)
    : RecyclerView.Adapter<ProductHistoryAdapter.ProductHistoryHolder>() {

    var mapTransaction : MutableMap<Model.Transaction,Model.Contact> = mutableMapOf()
    var transactions : MutableList<Model.Transaction> = mutableListOf()

    fun updateTransaction(newData : MutableMap<Model.Transaction,Model.Contact>,
                          newTransactions :MutableList<Model.Transaction>){
        mapTransaction = newData
        transactions = newTransactions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHistoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_history,parent,false)
        return ProductHistoryHolder(view)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: ProductHistoryHolder, position: Int) {
        holder.bind(transactions[position],mapTransaction[transactions[position]]!!,listener)
    }


    class ProductHistoryHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        val txtCompany = itemView?.findViewById<TxtMedium>(R.id.txtCompany)
        val txtPriceSale = itemView?.findViewById<TxtMedium>(R.id.txtPriceSale)
        val txtVat = itemView?.findViewById<TxtMedium>(R.id.txtVat)
        val txtValues = itemView?.findViewById<TxtMedium>(R.id.txtValues)
        val txtDate = itemView?.findViewById<TxtMedium>(R.id.txtDate)

        fun bind(transaction: Model.Transaction,contact:Model.Contact,listener:ProductHistoryOnClickListener){
            txtCompany?.text = contact.company
            if (transaction.sale_price.isNotEmpty()) {
                txtPriceSale?.text = transaction.sale_price[0].price
                txtVat?.text = when(transaction.sale_price[0].vat){
                    ROOT.VAT ->{
                        "A"
                    }
                    ROOT.NOVAT ->{
                        "B"
                    }
                    ROOT.CASH ->{
                        "C"
                    }
                    else ->{
                        ""
                    }
                }
                txtValues?.text = transaction.sale_price[0].values
                txtDate?.text = transaction.date
            }

            txtCompany?.setOnClickListener {
                listener.OnClickHistoryProduct(transaction)
            }
            txtPriceSale?.setOnClickListener {
                listener.OnClickHistoryProduct(transaction)
            }
            txtVat?.setOnClickListener {
                listener.OnClickHistoryProduct(transaction)
            }
            txtValues?.setOnClickListener {
                listener.OnClickHistoryProduct(transaction)
            }
            txtDate?.setOnClickListener {
                listener.OnClickHistoryProduct(transaction)
            }



        }
    }

    interface ProductHistoryOnClickListener{
        fun OnClickHistoryProduct(transaction: Model.Transaction)
    }


}