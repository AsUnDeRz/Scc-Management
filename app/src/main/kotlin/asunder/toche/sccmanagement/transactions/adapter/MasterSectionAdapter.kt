package asunder.toche.sccmanagement.transactions.adapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.transactions.TransactionListener
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

/**
 *Created by ToCHe on 22/3/2018 AD.
 */
class MasterSectionAdapter(var masterGroup: Model.MasterGroup)
    : RecyclerView.Adapter<MasterSectionAdapter.MasterHolder>() {

    fun updateMasterGroup(newData:Model.MasterGroup){
        masterGroup =  newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasterHolder {
        val view = LayoutInflater.from(parent.context).
                inflate(R.layout.item_master_section,parent,false)
        return MasterHolder(view)
    }

    override fun getItemCount(): Int {
        return masterGroup.groupDate.size
    }

    override fun onBindViewHolder(holder: MasterHolder, position: Int) {
        val data =masterGroup.resultMap[masterGroup.groupDate[position]]
        if (data != null) {
            holder.bindSection(data,masterGroup.groupDate[position])
        }
    }


    class MasterHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val txtGroupDate = itemView?.findViewById<TxtMedium>(R.id.txtGroupDate)
        val rvSection = itemView?.findViewById<RecyclerView>(R.id.rvSection)

        fun bindSection(data : SectionedRecyclerViewAdapter,date:String){
            txtGroupDate?.text = "วันที่ : $date"
            rvSection?.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                setHasFixedSize(true)
                adapter = data
            }
        }
    }
}