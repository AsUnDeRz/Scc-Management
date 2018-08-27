package asunder.toche.sccmanagement.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.multiselect.ComponentMultiSelect
import asunder.toche.sccmanagement.custom.multiselect.SelectHolder
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import com.bignerdranch.android.multiselector.MultiSelector

/**
 *Created by ToCHe on 14/8/2018 AD.
 */
class SelectDataAdapter(val listener: ComponentMultiSelect):
        RecyclerView.Adapter<SelectDataAdapter.SelectDataHolder>() {

    val contacts:MutableList<Model.Contact> = mutableListOf()
    val products:MutableList<Model.Product> = mutableListOf()
    val mMultiSelector = MultiSelector()
    var isContact = true

    fun updateIsContact(data:Boolean){
        isContact = data
        notifyDataSetChanged()
    }

    fun updateContactData(data:MutableList<Model.Contact>){
        contacts.clear()
        contacts.addAll(data)
        notifyDataSetChanged()
    }

    fun addContact(data:Model.Contact){
        contacts.add(data)
        notifyDataSetChanged()
    }

    fun updateProductData(data:MutableList<Model.Product>){
        products.clear()
        products.addAll(data)
        notifyDataSetChanged()
    }

    fun addProduct(data:Model.Product){
        products.add(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectDataHolder {
        return if (isContact){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_company,parent,false)
            SelectDataHolder(view,mMultiSelector)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_new,parent,false)
            SelectDataHolder(view,mMultiSelector)
        }

    }

    override fun getItemCount(): Int {
        return if (isContact){
            contacts.size
        }else {
            products.size
        }
    }

    fun getSizeData():Int{
        return if (isContact){
            contacts.size
        }else {
            products.size
        }
    }

    override fun onBindViewHolder(holder: SelectDataHolder, position: Int) {
        if (isContact){
            holder.bindContact(contacts[position])
        }else{
            holder.bindProduct(products[position])
        }
    }


    inner class SelectDataHolder(itemView: View, multiSelector: MultiSelector?)
        : SelectHolder(itemView, multiSelector), View.OnClickListener, View.OnLongClickListener {
        val txtCompany = itemView.findViewById<TxtMedium>(R.id.txtCompany)
        val txtProdct =itemView.findViewById<TxtMedium>(R.id.txtProduct)
        val txtPriceMedium = itemView.findViewById<TxtMedium>(R.id.txtPriceMedium)
        val txtVat = itemView.findViewById<TxtMedium>(R.id.txtVat)
        val txtDate = itemView.findViewById<TxtMedium>(R.id.txtDate)

        override fun onClick(v: View?) {
            if (!mMultiSelector.tapSelection(this)) {
                if (isContact){
                    listener.onComponentClicked(contacts[adapterPosition])
                }else{
                    listener.onComponentClicked(products[adapterPosition])
                }
            }else{
                listener.onComponentSelected()
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (!mMultiSelector.isSelectable) {
                mMultiSelector.isSelectable = true
                mMultiSelector.setSelected(this, true)
                listener.onUserEnableMultiSelect()
                return true
            }
            return false
        }

        fun bindContact(data :Model.Contact){
            txtCompany.text = data.company
            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)
        }

        fun bindProduct(product :Model.Product){
            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)
            txtProdct?.text = product.product_name
            if (product.medium_rate.isNotEmpty()){
                txtPriceMedium?.text = checkDecimal(product.medium_rate[0].price)
                if(product.medium_rate[0].vat) {
                    txtVat?.text = "A"
                }else{
                    txtVat?.text = "B"
                }
            }else{
                txtPriceMedium?.text = ""
                txtVat?.text = ""
            }

            txtDate?.text = product.date.substring(0,10)
        }

        fun checkDecimal(price:String):String {
            return if (price.length > 2) {
                if (price[price.lastIndex - 2].toString() == ".") {
                    if (price[price.lastIndex].toString() != "0" || price[price.lastIndex - 1].toString() != "0") {
                        price
                    } else {
                        price.toDouble().toInt().toString()
                    }
                } else {
                    price
                }
            }else{
                price
            }
        }
    }
}