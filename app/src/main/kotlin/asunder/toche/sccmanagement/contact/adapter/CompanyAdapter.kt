package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class CompanyAdapter(private val editAble:Boolean) : RecyclerView.Adapter<CompanyHolder>() {

    var contacts : MutableList<Model.Contact> = mutableListOf()
    lateinit var listener : CompanyListener
    lateinit var onClickListener : CompanyOnClickListener

    fun setContact(model :MutableList<Model.Contact>){
        this.contacts = model
        this.contacts.sortBy { it.company }
        this.contacts.forEach {
            Log.d("Company",it.toString())
        }
        notifyDataSetChanged()
    }
    fun setUpListener(listen: CompanyListener){
        listener = listen
    }

    fun setUpOnClickListener(listener: CompanyOnClickListener){
        onClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_company,parent,false)
        return CompanyHolder(view)
    }

    override fun getItemCount(): Int {
        return contacts.size

    }

    override fun onBindViewHolder(holder: CompanyHolder, position: Int) {
        if(editAble) {
            holder.bindWithEditable(contacts[position], listener)
        }else{
            holder.bind(contacts[position],onClickListener)
        }
    }

    interface CompanyListener{
        fun onSelectContact(contact: Model.Contact)
        fun onClickEdit(contact: Model.Contact)
        fun onClickDelete(contact: Model.Contact)
    }
    interface CompanyOnClickListener{
        fun onClickCompany(contact: Model.Contact)
    }
}