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
import asunder.toche.sccmanagement.products.adapter.ProductAdapter
import kotlinx.android.synthetic.main.fragment_product_list.*

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductListFragment : Fragment(){


    lateinit var productAdapter : ProductAdapter
    companion object {
        fun newInstance(): ProductListFragment = ProductListFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_product_list,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()

    }

    fun setupAdapter(){
        val data = mutableListOf<Model.Product>()
        val mediumRate = mutableListOf<Model.MediumRate>()
        for (i in 0 until 3){
            mediumRate.add(Model.MediumRate("154,465",true,"10","","",
                    true))
        }
        for (i in 0 until 10){
            data.add(Model.Product("","Tootsd $i","","",
                    "","", Utils.getCurrentDateShort(),mediumRate))
        }
        productAdapter = ProductAdapter(data)
        rvProduct.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = productAdapter
        }
    }
}