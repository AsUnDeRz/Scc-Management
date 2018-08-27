package asunder.toche.sccmanagement.products.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.preference.Utils
import com.bumptech.glide.Glide

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class MediumRateAdapter(var mediumList: MutableList<Model.MediumRate>,val listener: MediumListener?) :
        RecyclerView.Adapter<MediumRateAdapter.MediumRateHolder>() {

    fun updateMedium(position: Int,mediumRate: Model.MediumRate){
        mediumList.removeAt(position)
        mediumList.add(position,mediumRate)
        sortData()
        notifyDataSetChanged()
    }

    fun deleteMedium(position: Int){
        mediumList.removeAt(position)
        sortData()
        notifyDataSetChanged()
    }


    fun updateMediumList(newData : MutableList<Model.MediumRate>){
        mediumList.clear()
        mediumList.addAll(newData)
        //mediumList.sortByDescending { Utils.getDateWithString(it.date).time }
        sortData()
        notifyDataSetChanged()
    }

    fun addMediumList(mediumRate: Model.MediumRate){
        if(mediumList.size > 0) {
            mediumList.forEach {
                it.default = false
            }
        }
        mediumList.add(0,mediumRate)
        //mediumList.sortByDescending { Utils.getDateWithString(it.date).time }
        if (mediumList.isNotEmpty()){
            if (mediumList.size > 20){
                mediumList.removeAt(mediumList.lastIndex)
            }
        }
        sortData()
        notifyDataSetChanged()
    }

    fun deleteMediumList(mediumRate: Model.MediumRate){
        mediumList.remove(mediumRate)
        notifyDataSetChanged()
    }

    fun sortData(){
        mediumList.sortByDescending { Utils.getDateWithString(it.date).time }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediumRateHolder {
        val view = LayoutInflater.from(parent.context).
                inflate(R.layout.item_medium_rate,parent,false)
        return MediumRateHolder(view)
    }

    override fun getItemCount(): Int {
        return  mediumList.size
    }

    override fun onBindViewHolder(holder: MediumRateHolder, position: Int) {
        holder.bind(mediumList[position])
    }


    inner class MediumRateHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        private val txtMediumPrice = itemView?.findViewById<TxtMedium>(R.id.txtMediumPrice)
        private val txtMediumVat = itemView?.findViewById<TxtMedium>(R.id.txtMediumVat)
        private val txtMediumDate = itemView?.findViewById<TxtMedium>(R.id.txtMediumDate)
        private val txtMediumNote = itemView?.findViewById<TxtMedium>(R.id.txtMediumNote)

        fun bind(mediumRate: Model.MediumRate){
            txtMediumPrice?.text = mediumRate.price
            txtMediumDate?.text = Utils.format2DigiYMD(mediumRate.date)
            txtMediumVat?.text = if(mediumRate.vat) "A" else "B"
            txtMediumNote?.text = mediumRate.note

            itemView?.setOnClickListener {
                listener?.onClickMedium(mediumRate,adapterPosition)
            }

        }

    }

    interface MediumListener{
        fun onClickMedium(mediumRate: Model.MediumRate,position:Int)
        fun onClickLongMedium(mediumRate: Model.MediumRate)
    }
}