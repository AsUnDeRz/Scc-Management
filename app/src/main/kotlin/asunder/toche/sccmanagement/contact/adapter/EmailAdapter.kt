package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ComponentListener
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import com.bumptech.glide.Glide

/**
 *Created by ToCHe on 3/4/2018 AD.
 */
class EmailAdapter(var listener: ComponentListener): RecyclerView.Adapter<EmailAdapter.EmailHolder>(){

    val emails:MutableList<String> =  mutableListOf()

    fun updateEmails(data:MutableList<String>){
        emails.clear()
        emails.addAll(data)
        notifyDataSetChanged()
    }

    fun addEmail(data:String){
        emails.add(data)
        notifyDataSetChanged()
    }

    fun remove(position: Int){
        emails.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_component_info,parent,false)
        return EmailHolder(view)
    }

    override fun getItemCount(): Int {
        return emails.size
    }

    override fun onBindViewHolder(holder: EmailHolder, position: Int) {
        holder.bind(emails[position],listener)
    }


    inner class EmailHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val txtTitle = itemView?.findViewById<TxtMedium>(R.id.txtTitle)
        val edtContent = itemView?.findViewById<EdtMedium>(R.id.edtContent)
        val imageStateDelete = itemView?.findViewById<ImageView>(R.id.imageStateDelete)
        val imageAction = itemView?.findViewById<AppCompatImageView>(R.id.imageAction)

        fun bind(email: String,listener: ComponentListener){
            txtTitle?.text = "อีเมล"
            edtContent?.setText(email)
            imageStateDelete?.isSelected = true
            imageStateDelete?.setOnClickListener {
                listener.OnEmailClick(emails[adapterPosition],false,adapterPosition)
            }
            imageAction?.setOnClickListener {
                listener.OnEmailClick(emails[adapterPosition],true,adapterPosition)
            }
            imageStateDelete?.let {
                Glide.with(itemView.context)
                        .load(R.drawable.ic_remove_white_24dp)
                        .into(it)
            }
            imageAction?.let {
                Glide.with(itemView.context)
                        .load(android.R.drawable.ic_dialog_email)
                        .into(it)
            }
            edtContent?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(!s.isNullOrEmpty() && !s.isNullOrBlank()){
                        emails[adapterPosition] = s.toString()
                    }else{
                        emails[adapterPosition] = ""
                    }
                }
            })

        }
    }
}