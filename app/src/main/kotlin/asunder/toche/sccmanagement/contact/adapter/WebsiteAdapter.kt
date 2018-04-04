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
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import com.bumptech.glide.Glide

/**
 *Created by ToCHe on 3/4/2018 AD.
 */
class WebsiteAdapter(var listener: ComponentListener): RecyclerView.Adapter<WebsiteAdapter.WebSiteHolder>(){

    val websites:MutableList<String> =  mutableListOf()

    fun updateWebsites(data:MutableList<String>){
        websites.clear()
        websites.addAll(data)
        notifyDataSetChanged()
    }

    fun addWebSite(data:String){
        websites.add(data)
        notifyDataSetChanged()
    }

    fun remove(position: Int){
        websites.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebSiteHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_component_info,parent,false)
        return WebSiteHolder(view)
    }

    override fun getItemCount(): Int {
        return websites.size
    }

    override fun onBindViewHolder(holder: WebSiteHolder, position: Int) {
        holder.bind(websites[position],listener)
    }


    inner class WebSiteHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        val txtTitle = itemView?.findViewById<TxtMedium>(R.id.txtTitle)
        val edtContent = itemView?.findViewById<EdtMedium>(R.id.edtContent)
        val imageStateDelete = itemView?.findViewById<ImageView>(R.id.imageStateDelete)
        val imageAction = itemView?.findViewById<AppCompatImageView>(R.id.imageAction)


        fun bind(website: String,listener: ComponentListener){
            txtTitle?.text = "เว็ปไซด์"
            edtContent?.setText(website)
            imageStateDelete?.isSelected = true
            imageStateDelete?.setOnClickListener {
                listener.OnWebsiteClick(websites[adapterPosition],false,adapterPosition)
            }

            imageAction?.setOnClickListener {
                listener.OnWebsiteClick(websites[adapterPosition],true,adapterPosition)
            }
            imageStateDelete?.let {
                Glide.with(itemView.context)
                        .load(R.drawable.ic_remove_white_24dp)
                        .into(it)
            }
            imageAction?.let {
                Glide.with(itemView.context)
                        .load(android.R.drawable.ic_menu_manage)
                        .into(it)
            }

            edtContent?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(!s.isNullOrEmpty() && !s.isNullOrBlank()){
                        websites[adapterPosition] = s.toString()
                    }else{
                        websites[adapterPosition] = ""
                    }
                }
            })
        }
    }
}