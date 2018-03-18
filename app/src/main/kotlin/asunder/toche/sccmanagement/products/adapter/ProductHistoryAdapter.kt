package asunder.toche.sccmanagement.products.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import kotlinx.android.synthetic.main.item_product_history.view.*

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductHistoryAdapter(var transactions:MutableList<Model.Transaction>) : RecyclerView.Adapter<ProductHistoryAdapter.ProductHistoryHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHistoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_history,parent,false)
        return ProductHistoryHolder(view)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: ProductHistoryHolder, position: Int) {
        holder.bind(transactions[position])
    }


    class ProductHistoryHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        val txtCompany = itemView?.findViewById<TxtMedium>(R.id.txtCompany)
        val txtPriceSale = itemView?.findViewById<TxtMedium>(R.id.txtPriceSale)
        val txtVat = itemView?.findViewById<TxtMedium>(R.id.txtVat)
        val txtValues = itemView?.findViewById<TxtMedium>(R.id.txtValues)
        val txtDate = itemView?.findViewById<TxtMedium>(R.id.txtDate)

        fun bind(transaction : Model.Transaction){
            txtCompany?.text = findCompany()
            txtPriceSale?.text = transaction.sale_price[0].price
            if(transaction.sale_price[0].vat) {
                txtVat?.text = "A"
            }else{
                txtVat?.text = "B"
            }
            txtValues?.text = transaction.sale_price[0].values
            txtDate?.text = transaction.date

        }

        fun findCompany() : String{
            return "Company"
        }


    }
}