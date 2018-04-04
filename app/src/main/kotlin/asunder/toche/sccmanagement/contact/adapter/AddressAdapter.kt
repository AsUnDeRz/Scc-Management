package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ComponentListener
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import com.bumptech.glide.Glide

/**
 *Created by ToCHe on 3/4/2018 AD.
 */
class AddressAdapter(var listener : ComponentListener): RecyclerView.Adapter<AddressAdapter.AddressHolder>(){

    val addresses:MutableList<String> =  mutableListOf()

    fun updateAddress(data:MutableList<String>){
        addresses.clear()
        addresses.addAll(data)
        notifyDataSetChanged()
    }

    fun addAddress(data:String){
        addresses.add(data)
        notifyDataSetChanged()
    }

    fun remove(position: Int){
        addresses.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_component_info,parent,false)
        return AddressHolder(view)
    }

    override fun getItemCount(): Int {
        return addresses.size
    }

    override fun onBindViewHolder(holder: AddressHolder, position: Int) {
        holder.bind(addresses[position],listener)
    }


    inner class AddressHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val txtTitle = itemView?.findViewById<TxtMedium>(R.id.txtTitle)
        val edtContent = itemView?.findViewById<EdtMedium>(R.id.edtContent)
        val imageStateDelete = itemView?.findViewById<ImageView>(R.id.imageStateDelete)
        val imageAction = itemView?.findViewById<AppCompatImageView>(R.id.imageAction)

        fun bind(address: String,listener: ComponentListener){
            imageAction?.visibility = View.GONE
            txtTitle?.text = "ที่อยู่"
            edtContent?.setText(address)
            imageStateDelete?.isSelected = true
            imageStateDelete?.setOnClickListener {
                listener.OnAddressClick(addresses[adapterPosition],false,adapterPosition)
            }
            edtContent?.DisableClick()
            edtContent?.setOnClickListener {
                listener.OnAddressClick(addresses[adapterPosition],true,adapterPosition)
            }
            imageStateDelete?.let {
                Glide.with(itemView.context)
                        .load(R.drawable.ic_remove_white_24dp)
                        .into(it)
            }
            edtContent?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(!s.isNullOrEmpty() && !s.isNullOrBlank()){
                        addresses[adapterPosition] = s.toString()
                    }else{
                        addresses[adapterPosition] = ""
                    }
                }
            })
        }
    }
}