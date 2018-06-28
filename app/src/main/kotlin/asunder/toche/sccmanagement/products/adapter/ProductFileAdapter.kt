package asunder.toche.sccmanagement.products.adapter

import android.net.Uri
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.products.ComponentListener
import com.bumptech.glide.Glide
import java.io.File

/**
 *Created by ToCHe on 4/5/2018 AD.
 */
class ProductFileAdapter(var listener: ComponentListener): RecyclerView.Adapter<ProductFileAdapter.FileHolder>(){

    val files:MutableList<Model.ContentForProduct> =  mutableListOf()
    val typeList:MutableList<String> = mutableListOf()
    val defaultType = arrayListOf("Home","Work","Mobile")

    fun updateFiles(data:MutableList<Model.ContentForProduct>){
        files.clear()
        files.addAll(data)
        notifyDataSetChanged()
    }
    fun addFiles(data:MutableList<Model.ContentForProduct>){
        files.addAll(data)
        notifyDataSetChanged()
    }

    fun addFile(data:Model.ContentForProduct){
        files.add(data)
        notifyDataSetChanged()
    }

    fun remove(position: Int){
        files.removeAt(position)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_file,parent,false)
        return FileHolder(view)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        holder.bind(files[position],listener)
    }


    inner class FileHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val txtTitle = itemView?.findViewById<TxtMedium>(R.id.txtTitle)
        val edtContent = itemView?.findViewById<TxtMedium>(R.id.txtFile)
        val imageStateDelete = itemView?.findViewById<ImageView>(R.id.imageStateDelete)

        fun bind(content:Model.ContentForProduct,listener: ComponentListener){
            val file = Uri.fromFile(File(content.local_path))
            if(content.title_type == "" || content.title_type == "เลือก"){
                txtTitle?.text = "เลือก"
            }else {
                txtTitle?.text = content.title_type
            }
            edtContent?.text = file?.lastPathSegment
            imageStateDelete?.isSelected = true
            imageStateDelete?.setOnClickListener {
                listener.OnFileClick(files[adapterPosition],true,adapterPosition)
            }
            edtContent?.setOnClickListener {
                listener.OnFileClick(files[adapterPosition],false,adapterPosition)
            }
            imageStateDelete?.let {
                Glide.with(itemView.context)
                        .load(R.drawable.ic_remove_white_24dp)
                        .into(it)
            }

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
                files[itemPosition].title_type = parent.getItemAtPosition(position) as String
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