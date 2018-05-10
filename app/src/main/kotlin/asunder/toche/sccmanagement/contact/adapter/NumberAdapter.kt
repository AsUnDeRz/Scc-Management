package asunder.toche.sccmanagement.contact.adapter

import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ComponentListener
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import com.bumptech.glide.Glide
import android.widget.ArrayAdapter
import android.widget.ListView
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.extension.limitLength
import asunder.toche.sccmanagement.preference.Utils


/**
 *Created by ToCHe on 3/4/2018 AD.
 */
class NumberAdapter(var listener :ComponentListener):RecyclerView.Adapter<NumberAdapter.NumberHolder>(){

    val numbers:MutableList<Model.Channel> =  mutableListOf()
    val typeList:MutableList<String> = mutableListOf()
    val defaultType = arrayListOf("Home","Work","Mobile")

    fun updateNumbers(data:MutableList<Model.Channel>){
        numbers.clear()
        numbers.addAll(data)
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

    fun addNumber(data:Model.Channel){
        numbers.add(data)
        notifyDataSetChanged()
    }
    fun remove(position: Int){
        numbers.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_component_info,parent,false)
        return NumberHolder(view)
    }

    override fun getItemCount(): Int {
        return numbers.size
    }

    override fun onBindViewHolder(holder: NumberHolder, position: Int) {
        holder.bind(numbers[position],listener)
    }


    inner class NumberHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        val txtTitle = itemView?.findViewById<TxtMedium>(R.id.txtTitle)
        val edtContent = itemView?.findViewById<EdtMedium>(R.id.edtContent)
        val imageStateDelete = itemView?.findViewById<ImageView>(R.id.imageStateDelete)
        val imageAction = itemView?.findViewById<AppCompatImageView>(R.id.imageAction)

        fun bind(number: Model.Channel,listener:ComponentListener){
            if(number.type == "" || number.type == "เลือก"){
                txtTitle?.text = "เลือก"
            }else {
                txtTitle?.text = number.type
            }
            edtContent?.setText(number.data)
            edtContent?.inputType = InputType.TYPE_CLASS_PHONE

            //edtContent?.limitLength(10)
            imageStateDelete?.isSelected = true
            imageStateDelete?.setOnClickListener {
                listener.OnNumberClick(numbers[adapterPosition],false,adapterPosition)
            }

            imageAction?.setOnClickListener {
                listener.OnNumberClick(numbers[adapterPosition],true,adapterPosition)
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
                        numbers[adapterPosition].data = s.toString()
                    }else{
                        numbers[adapterPosition].data = ""
                    }
                }
            })

            txtTitle?.setOnClickListener {
                showSheetCompany(adapterPosition)
            }
        }

        fun showSheetCompany(itemPosition: Int){
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
                numbers[itemPosition].type = parent.getItemAtPosition(position) as String
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