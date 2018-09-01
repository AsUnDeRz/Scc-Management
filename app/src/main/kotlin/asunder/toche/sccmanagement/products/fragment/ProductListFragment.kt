package asunder.toche.sccmanagement.products.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.main.ControlViewModel
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.products.ProductState
import asunder.toche.sccmanagement.products.adapter.ProductAdapter
import asunder.toche.sccmanagement.products.viewmodel.ProductViewModel
import kotlinx.android.synthetic.main.fragment_product_list.*

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductListFragment : Fragment(),ProductAdapter.ProductListener{

    lateinit var productAdapter : ProductAdapter
    private lateinit var productViewModel: ProductViewModel
    private lateinit var controlViewModel : ControlViewModel

    companion object {
        fun newInstance(): ProductListFragment = ProductListFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productViewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_product_list,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observProductViewModel()

    }

    fun setupAdapter(){
        val data = mutableListOf<Model.Product>()
        productAdapter = ProductAdapter(data,true)
        productAdapter.setUpListener(this)
        rvProduct.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = productAdapter
        }
        productViewModel.loadProduct()
    }


    fun observProductViewModel(){

        productViewModel.products.observe(this, Observer {
            it?.let {
                productAdapter.updateProduct(it)
            }
        })

        productViewModel.stateView.observe(this, Observer {
            when(it){
                ProductState.SHOWLIST ->{
                }
                ProductState.SHOWFORM ->{

                }
                ProductState.SHOWINPUT ->{

                }
                ProductState.SHOWMEDIUMFORM ->{

                }
                ProductState.SHOWPRODUCT ->{

                }
            }
        })

        controlViewModel.currentUI.observe(this, Observer {
            when(it){
                ROOT.PRODUCTS ->{
                    setupAdapter()
                }
            }
        })

    }



    override fun onSelectProduct(product: Model.Product) {
        productViewModel.updateProduct(product)
        productViewModel.updateStateView(ProductState.SHOWFORMWITHPRODUCT)
        //productViewModel.updateProduct(product)
        //productViewModel.updateStateView(ProductState.SHOWPRODUCT)
    }

    override fun onClickEdit(product: Model.Product) {
        productViewModel.updateProduct(product)
        productViewModel.updateStateView(ProductState.SHOWFORMWITHPRODUCT)

    }

    override fun onClickDelete(product: Model.Product) {
        productViewModel.deleteProduct(product)
    }

}