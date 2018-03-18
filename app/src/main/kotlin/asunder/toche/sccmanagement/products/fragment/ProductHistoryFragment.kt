package asunder.toche.sccmanagement.products.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.adapter.ProductHistoryAdapter
import kotlinx.android.synthetic.main.fragment_product_history.*

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductHistoryFragment : Fragment(){

    companion object {
        fun newInstance(): ProductHistoryFragment = ProductHistoryFragment()
    }

    lateinit var productHistoryAdapter: ProductHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_product_history,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()

    }

    fun setupAdapter(){
        val data = mutableListOf<Model.Transaction>()
        val price = mutableListOf<Model.SalePrice>()
        for (i in 0 until 3){
            price.add(Model.SalePrice("1$i",true,"50$i", Utils.getCurrentDateShort(),""))
        }
        for (i in 0 until 10){
            data.add(Model.Transaction("","","",""
                    ,Utils.getCurrentDateShort(),"",price))
        }
        productHistoryAdapter = ProductHistoryAdapter(data)
        rvCompanyPrefer.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = productHistoryAdapter
        }
    }
}