package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtThin
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.transactions.TransactionListener

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class HistoryTransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtProdName = itemView.findViewById<TxtThin>(R.id.txtProdName)
    private val txtSalePrice = itemView.findViewById<TxtThin>(R.id.txtSalePrice)
    private val txtSaleVat = itemView.findViewById<TxtThin>(R.id.txtSaleVat)
    private val txtSaleValues = itemView.findViewById<TxtThin>(R.id.txtSaleValues)
    private val txtSaleDate = itemView.findViewById<TxtThin>(R.id.txtSaleDate)
    private val txtMediumPrice = itemView.findViewById<TxtThin>(R.id.txtMediumPrice)
    private val txtMediumVat = itemView.findViewById<TxtThin>(R.id.txtMediumVat)
    private val txtMediumDate = itemView.findViewById<TxtThin>(R.id.txtMediumDate)
    private val txtNote = itemView.findViewById<TxtThin>(R.id.txtNote)


    fun bind(transaction: Model.Transaction,product: Model.Product,listener: TransactionListener?){
        txtProdName.text = product.product_name.lines().first()
        txtNote.text = transaction.desc
        if (transaction.sale_price.isNotEmpty()) {
            txtSalePrice.text = checkDecimal(transaction.sale_price[0].price)
            txtSaleVat.text = when(transaction.sale_price[0].vat){
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

            txtSaleValues.text = transaction.sale_price[0].values
            txtSaleDate.text = Utils.format2DigiYMD(transaction.sale_price[0].date)

        }

        if(transaction.medium_price.isNotEmpty()) {
            if (product.medium_rate.isNotEmpty()) {
                if (product.medium_rate[0].vat) {
                    txtMediumVat.text = "A"
                } else {
                    txtMediumVat.text = "B"
                }
                txtMediumPrice.text = product.medium_rate[0].price
                txtMediumDate.text = Utils.format2DigiYMD(product.medium_rate[0].date)
            }
        }

        itemView.rootView.setOnClickListener {
            listener?.onClickTransaction(transaction)
        }
        itemView.rootView.setOnLongClickListener {
            listener?.onClickNote(transaction.desc)
            return@setOnLongClickListener true
        }

    }

    fun checkDecimal(price:String):String{
        return if(price.length > 2){
            if (price[price.lastIndex].toString() != "0" || price[price.lastIndex-1].toString() != "0"
                    || price[price.lastIndex-2].toString() == "."){
                price
            }else{
                price.toDouble().toInt().toString()
            }
        }else{
            price
        }
    }

}