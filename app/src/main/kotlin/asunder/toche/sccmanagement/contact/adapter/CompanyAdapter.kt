package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class CompanyAdapter : RecyclerView.Adapter<CompanyHolder>() {

    var contacts : MutableList<Model.Contact> = mutableListOf()

    fun setContact(model :MutableList<Model.Contact>){
        this.contacts = model
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CompanyHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_company,parent,false)
        return CompanyHolder(view)
    }

    override fun getItemCount(): Int {
        return contacts.size

    }

    override fun onBindViewHolder(holder: CompanyHolder?, position: Int) {
        holder?.bind(contacts[holder.adapterPosition])
    }
}