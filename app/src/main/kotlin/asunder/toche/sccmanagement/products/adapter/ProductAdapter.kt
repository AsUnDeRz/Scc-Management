package asunder.toche.sccmanagement.products.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import java.util.zip.Inflater

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductAdapter(var products:MutableList<Model.Product>) : RecyclerView.Adapter<ProductAdapter.ProductHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product,parent,false)
        return ProductHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        holder.bind(products[position])
    }


    class ProductHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        val txtProdct =itemView?.findViewById<TxtMedium>(R.id.txtProduct)
        val txtPriceMedium = itemView?.findViewById<TxtMedium>(R.id.txtPriceMedium)
        val txtVat = itemView?.findViewById<TxtMedium>(R.id.txtVat)
        val txtDate = itemView?.findViewById<TxtMedium>(R.id.txtDate)

        fun bind(product:Model.Product){
            txtProdct?.text = product.product_name
            txtPriceMedium?.text = product.medium_rate[0].price
            if(product.medium_rate[0].vat) {
                txtVat?.text = "A"
            }else{
                txtVat?.text = "B"
            }
            txtDate?.text = product.date


        }

    }
}