package asunder.toche.sccmanagement.transactions.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class SaleRateAdapter(var saleList: MutableList<Model.SalePrice>,val listener:SaleRateListener) :
        RecyclerView.Adapter<SaleRateAdapter.SaleHolder>(){

    fun updateSaleRate(position: Int,salePrice: Model.SalePrice){
        saleList.removeAt(position)
        saleList.add(position,salePrice)
        notifyDataSetChanged()
    }

    fun deleteSaleRate(position: Int){
        saleList.removeAt(position)
        notifyDataSetChanged()
    }


    fun updatesaleList(newData : MutableList<Model.SalePrice>){
        saleList = newData
        //saleList.sortByDescending { Utils.getDateWithString(it.date).time }
        notifyDataSetChanged()
    }

    fun addSaleList(salePrice: Model.SalePrice){
        saleList.add(0,salePrice)
        //saleList.sortByDescending { Utils.getDateWithString(it.date).time}
        if (saleList.isNotEmpty()){
            if (saleList.size > 20){
                saleList.removeAt(saleList.lastIndex)
            }
        }
        notifyDataSetChanged()
    }

    fun addSalePrice(salePrice: Model.SalePrice){
        saleList.add(salePrice)
        saleList.sortByDescending { it.date }
        notifyDataSetChanged()

    }

    fun updateSalePrice(newData:MutableList<Model.SalePrice>){
        saleList = newData
        saleList.sortByDescending { it.date }
        if (saleList.isNotEmpty()){
            if (saleList.size > 20){
                saleList.removeAt(saleList.lastIndex)
            }
        }
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


    inner class SaleHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        private val txtSalePrice = itemView?.findViewById<TxtMedium>(R.id.txtSalePrice)
        private val txtSaleVat = itemView?.findViewById<TxtMedium>(R.id.txtSaleVat)
        private val txtSaleValues = itemView?.findViewById<TxtMedium>(R.id.txtSaleValues)
        private val txtSaleDate = itemView?.findViewById<TxtMedium>(R.id.txtSaleDate)

        fun bind(salePrice: Model.SalePrice){
            txtSaleDate?.text = Utils.format2DigiYMD(salePrice.date)
            txtSalePrice?.text = salePrice.price
            txtSaleValues?.text = salePrice.values
            txtSaleVat?.text = getSaleType(salePrice.vat)

            itemView.setOnClickListener {
                listener.onClickSaleRate(salePrice,adapterPosition)
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

    interface SaleRateListener{
        fun onClickSaleRate(salePrice: Model.SalePrice,position:Int)
        fun onClickLongSaleRate(salePrice: Model.SalePrice)
    }
}