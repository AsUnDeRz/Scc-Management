package asunder.toche.sccmanagement.transactions.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class SaleRateAdapter(var saleList: MutableList<Model.SalePrice>) :
        RecyclerView.Adapter<SaleRateAdapter.SaleHolder>(){


    fun addSalePrice(salePrice: Model.SalePrice){
        saleList.add(salePrice)
        saleList.sortByDescending { it.date }
        notifyDataSetChanged()

    }

    fun updateSalePrice(newData:MutableList<Model.SalePrice>){
        saleList = newData
        saleList.sortByDescending { it.date }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleHolder {
        val view = LayoutInflater.from(parent.context).
                inflate(R.layout.item_sale_rate,parent,false)
        return SaleHolder(view)
    }

    override fun getItemCount(): Int {
        return if(saleList.size >= 5) 5 else saleList.size
    }

    override fun onBindViewHolder(holder: SaleHolder, position: Int) {
        holder.bind(saleList[position])
    }


    class SaleHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        private val txtSalePrice = itemView?.findViewById<TxtMedium>(R.id.txtSalePrice)
        private val txtSaleVat = itemView?.findViewById<TxtMedium>(R.id.txtSaleVat)
        private val txtSaleValues = itemView?.findViewById<TxtMedium>(R.id.txtSaleValues)
        private val txtSaleDate = itemView?.findViewById<TxtMedium>(R.id.txtSaleDate)

        fun bind(salePrice: Model.SalePrice){
            txtSaleDate?.text = salePrice.date.substring(0,10)
            txtSalePrice?.text = salePrice.price
            txtSaleValues?.text = salePrice.values
            txtSaleVat?.text = if(salePrice.vat) "A" else "B"

        }

    }
}