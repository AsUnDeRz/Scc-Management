package asunder.toche.sccmanagement.products.adapter

import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.AppCompatImageView
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
import com.google.firebase.storage.FirebaseStorage
import java.io.File

/**
 *Created by ToCHe on 4/5/2018 AD.
 */
class ProductPictureAdapter(var listener: ComponentListener): RecyclerView.Adapter<ProductPictureAdapter.PictureHolder>(){

    val pictures:MutableList<Model.ContentForProduct> =  mutableListOf()
    val typeList:MutableList<String> = mutableListOf()
    val defaultType = arrayListOf("Home","Work","Mobile")

    fun updatePictures(data:MutableList<Model.ContentForProduct>){
        pictures.clear()
        pictures.addAll(data)
        notifyDataSetChanged()
    }

    fun addPictures(data:MutableList<Model.ContentForProduct>){
        pictures.addAll(data)
        notifyDataSetChanged()
    }

    fun addPicture(data:Model.ContentForProduct){
        pictures.add(data)
        notifyDataSetChanged()
    }

    fun remove(position: Int){
        pictures.removeAt(position)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_picture,parent,false)
        return PictureHolder(view)
    }

    override fun getItemCount(): Int {
        return pictures.size
    }

    override fun onBindViewHolder(holder: PictureHolder, position: Int) {
        holder.bind(pictures[position],listener)
    }


    inner class PictureHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val txtTitle = itemView?.findViewById<TxtMedium>(R.id.txtTitle)
        val imageStateDelete = itemView?.findViewById<ImageView>(R.id.imageStateDelete)
        val imageAction = itemView?.findViewById<AppCompatImageView>(R.id.picture)

        fun bind(picture: Model.ContentForProduct,listener: ComponentListener){
            if(picture.title_type == "" || picture.title_type == "เลือก"){
                txtTitle?.text = "เลือก"
            }else {
                txtTitle?.text = picture.title_type
            }
            imageStateDelete?.isSelected = true
            imageStateDelete?.setOnClickListener {
                listener.OnPictureClick(pictures[adapterPosition],true,adapterPosition)
            }
            imageAction?.setOnClickListener {
                listener.OnPictureClick(pictures[adapterPosition],false,adapterPosition)
            }
            imageStateDelete?.let {
                Glide.with(itemView.context)
                        .load(R.drawable.ic_remove_white_24dp)
                        .into(it)
            }
            imageAction?.let { image ->
                val f = File(picture.local_path)
                if (f.exists()) {
                    Glide.with(itemView.context)
                            .load(File(picture.local_path))
                            .into(image)
                    println()
                }else{
                    FirebaseStorage.getInstance().reference.child(picture.cloud_url)
                            .downloadUrl
                            .addOnSuccessListener { result ->
                                Glide.with(itemView.context)
                                        .load(result)
                                        .into(image)
                            }.addOnFailureListener {
                                Glide.with(itemView.context)
                                        .load(R.drawable.mock_picture)
                                        .into(image)
                            }
                    println()
                }
            }

            txtTitle?.setOnClickListener {
                showSheetCompany(adapterPosition,txtTitle)
            }
        }

        fun showSheetCompany(itemPosition: Int,txtTitle: TxtMedium){
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
                pictures[itemPosition].title_type = parent.getItemAtPosition(position) as String
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