package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtThin
import asunder.toche.sccmanagement.transactions.TransactionListener

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class HistoryTransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtProdName = itemView.findViewById<TxtThin>(R.id.txtProdName)
    val txtSalePrice = itemView.findViewById<TxtThin>(R.id.txtSalePrice)
    val txtSaleVat = itemView.findViewById<TxtThin>(R.id.txtSaleVat)
    val txtSaleValues = itemView.findViewById<TxtThin>(R.id.txtSaleValues)
    val txtSaleDate = itemView.findViewById<TxtThin>(R.id.txtSaleDate)
    val txtMediumPrice = itemView.findViewById<TxtThin>(R.id.txtMediumPrice)
    val txtMediumVat = itemView.findViewById<TxtThin>(R.id.txtMediumVat)
    val txtMediumDate = itemView.findViewById<TxtThin>(R.id.txtMediumDate)

    fun bind(transaction: Model.Transaction,product: Model.Product,listener: TransactionListener){
        txtProdName.text = product.product_name
        txtSalePrice.text = transaction.sale_price[0].price
        if(transaction.sale_price[0].vat){
            txtSaleVat.text = "A"
        }else{
            txtSaleVat.text = "B"
        }
        if(product.medium_rate[0].vat){
            txtMediumVat.text = "A"
        }else{
            txtMediumVat.text = "B"
        }
        txtSaleValues.text = transaction.sale_price[0].values
        txtSaleDate.text = transaction.sale_price[0].date.substring(0,10)
        txtMediumPrice.text = product.medium_rate[0].price
        txtMediumDate.text = product.medium_rate[0].date.substring(0,10)

        itemView.setOnClickListener {
            listener.onClickTransaction(transaction)
        }
    }

}