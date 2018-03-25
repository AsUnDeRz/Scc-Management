package asunder.toche.sccmanagement.transactions.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.transactions.TransactionListener
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

/**
 *Created by ToCHe on 22/3/2018 AD.
 */
class SectionTransactionAdapter() : StatelessSection(SectionParameters.builder()
        .itemResourceId(R.layout.item_product_history)
        .headerResourceId(R.layout.item_section_issue)
        .build()) {
    constructor(companyName: String,
                data:MutableList<Model.Transaction>,
                mapProduct:MutableMap<String,Model.Product>,
                transactionListener: TransactionListener) : this(){
        sectionComp = companyName
        transactions = data
        products = mapProduct
        listener = transactionListener
    }
    lateinit var listener: TransactionListener
    var transactions: MutableList<Model.Transaction> = mutableListOf()
    var products: MutableMap<String,Model.Product> = mutableMapOf()
    var sectionComp:String =""

    override fun getContentItemsTotal(): Int {
        return transactions.size // number of items of this section
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        // return a custom instance of ViewHolder for the items of this section
        return ItemHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as ItemHolder
        itemHolder.bind(transactions[position],
                products[transactions[position].id]!!,
                listener)
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return SectionHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        val headerHolder = holder as SectionHolder
        headerHolder.sectionDate.text = sectionComp.trim()+" (${transactions.size})"
    }

    class SectionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionDate: TxtMedium = itemView.findViewById(R.id.txtSectionDate)
    }
    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val txtCompany = itemView.findViewById<TxtMedium>(R.id.txtCompany)
        val txtPriceSale = itemView.findViewById<TxtMedium>(R.id.txtPriceSale)
        val txtVat = itemView.findViewById<TxtMedium>(R.id.txtVat)
        val txtValues = itemView.findViewById<TxtMedium>(R.id.txtValues)
        val txtDate = itemView.findViewById<TxtMedium>(R.id.txtDate)

        fun bind(data: Model.Transaction,product:Model.Product,listener: TransactionListener){
            txtCompany.text = product.product_name
            txtDate.text = data.date
            txtVat.text = "A"
            txtPriceSale.text = data.sale_price[0].price
            txtValues.text = data.sale_price[0].values
            itemView.setOnClickListener {
                listener.onClickTransaction(data)
            }
        }
    }


}