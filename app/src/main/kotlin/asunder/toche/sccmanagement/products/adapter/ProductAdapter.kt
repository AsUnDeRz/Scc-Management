package asunder.toche.sccmanagement.products.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.R.id.btnDelete
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import me.thanel.swipeactionview.SwipeActionView
import me.thanel.swipeactionview.SwipeDirection
import me.thanel.swipeactionview.SwipeGestureListener
import java.util.zip.Inflater

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductAdapter(var products:MutableList<Model.Product>,var editAble:Boolean)
    : RecyclerView.Adapter<ProductAdapter.ProductHolder>() {


    lateinit var listener : ProductListener
    lateinit var onClickListener : ProductOnClickListener

    fun updateProduct(data:MutableList<Model.Product>){
        products = data
        products.sortBy { it.product_name }
        notifyDataSetChanged()
    }


    fun setUpListener(listener: ProductListener){
        this.listener = listener

    }
    fun setUpOnClickListener(listener: ProductOnClickListener){
        this.onClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product,parent,false)
        return ProductHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        if(editAble) {
            holder.bind(products[position], listener)
        }else{
            holder.bind(products[position],onClickListener)
        }
    }


    class ProductHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        val txtProdct =itemView?.findViewById<TxtMedium>(R.id.txtProduct)
        val txtPriceMedium = itemView?.findViewById<TxtMedium>(R.id.txtPriceMedium)
        val txtVat = itemView?.findViewById<TxtMedium>(R.id.txtVat)
        val txtDate = itemView?.findViewById<TxtMedium>(R.id.txtDate)
        val swipeView = itemView?.findViewById<SwipeActionView>(R.id.swipeProduct)


        fun bind(product:Model.Product,listener: ProductListener){
            val btnEdt = swipeView?.findViewById<BtnMedium>(R.id.btnEdit)
            val btnDelete = swipeView?.findViewById<BtnMedium>(R.id.btnDelete)
            txtProdct?.text = product.product_name
            txtPriceMedium?.text = product.medium_rate[0].price
            if(product.medium_rate[0].vat) {
                txtVat?.text = "A"
            }else{
                txtVat?.text = "B"
            }
            txtDate?.text = product.date.substring(0,10)
            swipeView?.swipeGestureListener = object : SwipeGestureListener {
                override fun onSwipedLeft(swipeActionView: SwipeActionView): Boolean {
                    btnDelete?.setOnClickListener {
                        listener.onClickDelete(product)
                    }
                    btnEdt?.setOnClickListener {
                        listener.onClickEdit(product)
                    }
                    return false
                }

                override fun onSwipedRight(swipeActionView: SwipeActionView): Boolean {
                    return true
                }
            }
            swipeView?.setOnClickListener {
                System.out.println("SwipeView OnClickListener")
            }


            itemView.setOnClickListener {
                listener.onSelectProduct(product)
            }
        }

        fun bind(product: Model.Product,listener:ProductOnClickListener){
            txtProdct?.text = product.product_name
            txtPriceMedium?.text = product.medium_rate[0].price
            if(product.medium_rate[0].vat) {
                txtVat?.text = "A"
            }else{
                txtVat?.text = "B"
            }
            txtDate?.text = product.date.substring(0,10)
            itemView.setOnClickListener {
                listener.onClickProduct(product)
            }
        }

    }

    interface ProductListener{
        fun onSelectProduct(product: Model.Product)
        fun onClickEdit(product: Model.Product)
        fun onClickDelete(product: Model.Product)
    }
    interface ProductOnClickListener{
        fun onClickProduct(product: Model.Product)
    }
}