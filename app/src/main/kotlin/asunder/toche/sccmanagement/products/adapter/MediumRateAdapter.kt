package asunder.toche.sccmanagement.products.adapter

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
class MediumRateAdapter(var mediumList: MutableList<Model.MediumRate>) :
        RecyclerView.Adapter<MediumRateAdapter.MediumRateHolder>() {



    fun updateMediumList(newData : MutableList<Model.MediumRate>){
        mediumList = newData
        mediumList.sortByDescending { it.date }
        notifyDataSetChanged()
    }

    fun addMediumList(mediumRate: Model.MediumRate){
        if(mediumList.size > 0) {
            mediumList.forEach {
                it.default = false
            }
        }
        mediumList.add(mediumRate)
        mediumList.sortByDescending { it.date }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediumRateHolder {
        val view = LayoutInflater.from(parent.context).
                inflate(R.layout.item_medium_rate,parent,false)
        return MediumRateHolder(view)
    }

    override fun getItemCount(): Int {
        return if(mediumList.size >= 5) 5 else mediumList.size
    }

    override fun onBindViewHolder(holder: MediumRateHolder, position: Int) {
        holder.bind(mediumList[position])
    }


    class MediumRateHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        private val txtMediumPrice = itemView?.findViewById<TxtMedium>(R.id.txtMediumPrice)
        private val txtMediumVat = itemView?.findViewById<TxtMedium>(R.id.txtMediumVat)
        private val txtMediumDate = itemView?.findViewById<TxtMedium>(R.id.txtMediumDate)

        fun bind(mediumRate: Model.MediumRate){
            txtMediumPrice?.text = mediumRate.price
            txtMediumDate?.text = mediumRate.date.substring(0,10)
            txtMediumVat?.text = if(mediumRate.vat) "A" else "B"

        }

    }
}