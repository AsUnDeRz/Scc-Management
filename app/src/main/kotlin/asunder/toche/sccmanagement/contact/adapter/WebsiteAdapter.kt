package asunder.toche.sccmanagement.contact.adapter

import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ComponentListener
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_component_info.view.*

/**
 *Created by ToCHe on 3/4/2018 AD.
 */
class WebsiteAdapter(var listener: ComponentListener): RecyclerView.Adapter<WebsiteAdapter.WebSiteHolder>(){

    val websites:MutableList<Model.Channel> =  mutableListOf()
    val typeList:MutableList<String> = mutableListOf()
    val defaultType = arrayListOf("Home","Work","Mobile")

    fun updateWebsites(data:MutableList<Model.Channel>){
        websites.clear()
        websites.addAll(data)
        notifyDataSetChanged()
    }

    fun addType(type:String){
        typeList.add(0,type)
        notifyDataSetChanged()
    }
    fun updateTypeList(data: MutableList<String>){
        typeList.clear()
        typeList.addAll(data)
    }
    fun addWebSite(data:Model.Channel){
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


    inner class WebSiteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtTitle = itemView.txtTitle
        val edtContent = itemView.edtContent
        val imageStateDelete = itemView.imageStateDelete
        val imageAction = itemView.imageAction


        fun bind(website: Model.Channel, listener: ComponentListener){
            if(website.type == "" || website.type == "เลือก"){
                txtTitle?.text = "เลือก"
            }else {
                txtTitle?.text = website.type
            }
            txtTitle.visibility = View.GONE
            edtContent?.setText(website.data)
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
                        .load(R.drawable.icon_www)
                        .into(it)
            }

            edtContent?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(!s.isNullOrEmpty() && !s.isNullOrBlank()){
                        websites[adapterPosition].data = s.toString()
                    }else{
                        websites[adapterPosition].data = ""
                    }
                }
            })

            txtTitle?.setOnClickListener {
                showSheetCompany(adapterPosition,txtTitle)
            }
        }

        fun showSheetCompany(itemPosition: Int,txtTitle:TxtMedium){
            val bottomSheetView = LayoutInflater.from(itemView.context).inflate(R.layout.bottom_sheet_product,null)
            val rvFilterType = bottomSheetView.findViewById<ListView>(R.id.rvFilterType)
            val btnCancel = bottomSheetView.findViewById<BtnMedium>(R.id.btnCancel)
            val btnAddType = bottomSheetView.findViewById<BtnMedium>(R.id.btnAddType)
            val edtType = bottomSheetView.findViewById<EdtMedium>(R.id.edtType)
            val bottomSheetDialog = BottomSheetDialog(itemView.context)
            bottomSheetDialog.setContentView(bottomSheetView)
            val sheetDisableCard = BottomSheetBehavior.from(bottomSheetView.parent as View)
            if (sheetDisableCard.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetDialog.show()

            } else {
                bottomSheetDialog.dismiss()
            }
            btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            val typeNumber = ArrayAdapter<String>(itemView.context,
                    android.R.layout.simple_list_item_1, android.R.id.text1, mutableListOf())
            typeNumber.addAll(typeList)
            typeNumber.addAll(defaultType)
            rvFilterType.adapter = typeNumber
            rvFilterType.setOnItemClickListener { parent, view, position, id ->
                websites[itemPosition].type = parent.getItemAtPosition(position) as String
                txtTitle.text = parent.getItemAtPosition(position) as String
                bottomSheetDialog.dismiss()
                notifyDataSetChanged()
            }

            btnAddType.setOnClickListener {
                if (!TextUtils.isEmpty(edtType.text)){
                    typeList.add(0,edtType.text.toString())
                    typeNumber.clear()
                    typeNumber.addAll(typeList)
                    typeNumber.addAll(defaultType)
                    listener.updateTypeList(edtType.text.toString())
                }
            }
        }

    }
}