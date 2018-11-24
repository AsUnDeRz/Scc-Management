package asunder.toche.sccmanagement.transactions.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
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
        transactions.sortBy { it.product_name }
    }
    constructor(companyName: String,
                data:MutableList<Model.Transaction>,
                transactionListener: TransactionListener) : this(){
        sectionComp = companyName
        transactions = data
        listener = transactionListener
        transactions.sortBy { it.product_name }
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
        products[transactions[position].id]?.let {
            itemHolder.bind(transactions[position],it,listener)
        }
        //itemHolder.bind(transactions[position],listener)
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
        val txtNote = itemView.findViewById<TxtMedium>(R.id.txtNote)

        fun bind(data: Model.Transaction,product:Model.Product,listener: TransactionListener){
            txtCompany.text = product.product_name?.lines()?.first()
            txtDate.text = Utils.format2DigiYMD(data.date)
            if (data.sale_price.isNotEmpty()) {
                txtVat.text = getSaleType(data.sale_price[0].vat)
                txtPriceSale.text = data.sale_price[0].price
                txtValues.text = data.sale_price[0].values
                txtNote.setText(data.sale_price[0].note)
            }
            itemView.setOnClickListener {
                listener.onClickTransaction(data)
            }
        }

        fun bind(data: Model.Transaction,listener: TransactionListener){
            txtCompany.text = data.product_name?.lines()?.first()
            txtDate.text = Utils.format2DigiYMD(data.date)
            if (data.sale_price.isNotEmpty()) {
                txtVat.text = getSaleType(data.sale_price[0].vat)
                txtPriceSale.text = data.sale_price[0].price
                txtValues.text = data.sale_price[0].values
                txtNote.setText(data.sale_price[0].note)
            }
            itemView.setOnClickListener {
                listener.onClickTransaction(data)
            }
        }
        fun getSaleType(typePrice:String):String{
            return when(typePrice){
                ROOT.NOVAT->{
                    "B"
                }
                ROOT.VAT ->{
                    "A"
                }
                ROOT.CASH->{
                    "C"
                }
                else ->{
                    ""
                }
            }
        }
    }




}