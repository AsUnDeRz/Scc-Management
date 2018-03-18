package asunder.toche.sccmanagement.products.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.ProductViewModel
import asunder.toche.sccmanagement.products.adapter.MediumRateAdapter
import asunder.toche.sccmanagement.products.pager.ProductsPager
import kotlinx.android.synthetic.main.fragment_product_add.*
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.layout_input.*
import kotlinx.android.synthetic.main.layout_price_rate.*
import kotlinx.android.synthetic.main.section_product_confirm.*
import kotlinx.android.synthetic.main.section_product_info.*

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class ProductsFragment : Fragment(){

    private val TAG = this::class.java.simpleName
    companion object {
        fun newInstance(): ProductsFragment = ProductsFragment()
    }
    lateinit var productViewModel:ProductViewModel
    private lateinit var rootLayoutInput : ScrollView
    private lateinit var rootLayoutMediumRate : ScrollView
    private lateinit var rootProductForm : ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productViewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_products,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPager()
        setUpTablayout()
        setEditAction()
        inflateStubProductAdd()
        inflateStubLayoutInput()
        inflateStubLayoutMediumRate()
    }


    fun inflateStubProductAdd(){
        stubProductAdd.setOnInflateListener { _, v ->
            rootProductForm = v as ConstraintLayout
            rootProductForm.visibility = View.GONE
        }
        stubProductAdd.inflate()
        initViewInStubProduct()

    }

    fun inflateStubLayoutInput(){
        stubLayoutInput.setOnInflateListener { _, v ->
            rootLayoutInput = v as ScrollView
            rootLayoutInput.visibility = View.GONE
        }
        stubLayoutInput.inflate()
        initViewInStubLayoutInput()

    }

    fun inflateStubLayoutMediumRate(){
        stubMediumRate.setOnInflateListener { _, v ->
            rootLayoutMediumRate = v as ScrollView
            rootLayoutMediumRate.visibility = View.GONE

        }
        stubMediumRate.inflate()
        initViewInStubMediumRate()
    }

    fun initViewInStubProduct(){
        imgNewMediumRate.setOnClickListener {
            showMediumPriceForm()
        }

        btnSaveProductOnTop.setOnClickListener {
            showLayoutInput()
        }

        btnCancelProduct.setOnClickListener {
            showProductList()
        }

        btnSaveProductOnBottom.setOnClickListener {
            showProductList()
        }

        rvMediumPrice.apply {
            val data : MutableList<Model.MediumRate> = mutableListOf()
            for(i in 0 until 10){
                data.add(Model.MediumRate("1000$i",true,"10$i",
                        Utils.getCurrentDateShort(),"",false))
            }
            data.sortByDescending { it.date }
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = MediumRateAdapter(data)
        }

    }

    fun initViewInStubLayoutInput(){
        btnCancelInput.setOnClickListener {
            showProductForm()
        }
    }

    fun initViewInStubMediumRate(){
        btnCancelRate.setOnClickListener {
            showProductForm()
        }

    }

    fun showProductList(){
        rootProductForm.visibility = View.GONE
        rootLayoutInput.visibility = View.GONE
        rootLayoutMediumRate.visibility = View.GONE
        imgNewProduct.visibility = View.VISIBLE

    }
    fun showProductForm(){
        productScrollView.fullScroll(ScrollView.FOCUS_UP)
        rootProductForm.visibility = View.VISIBLE
        rootLayoutInput.visibility = View.GONE
        rootLayoutMediumRate.visibility = View.GONE
        imgNewProduct.visibility = View.GONE

    }
    fun showLayoutInput(){
        rootLayoutInput.visibility = View.VISIBLE
        rootProductForm.visibility = View.GONE
        rootLayoutMediumRate.visibility = View.GONE
    }

    fun showMediumPriceForm(){
        rootLayoutInput.visibility = View.GONE
        rootProductForm.visibility = View.GONE
        rootLayoutMediumRate.visibility = View.VISIBLE

    }


    fun setUpPager(){
        Log.d(TAG,"SetupPager")
        vpProduct.adapter = ProductsPager(childFragmentManager)
        vpProduct.setAllowedSwipeDirection(CustomViewPager.SwipeDirection.none)
        vpProduct.offscreenPageLimit = 2


    }

    fun setUpTablayout(){
        Log.d(TAG,"SetupTablayoutr")
        tabProduct.setCustomSize(resources.getDimensionPixelSize(R.dimen.txt20).toFloat())
        tabProduct.setupWithViewPager(vpProduct)
        tabProduct.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1){
                    imgNewProduct.visibility = View.GONE
                }else{
                    imgNewProduct.visibility = View.VISIBLE
                }
            }
        })
    }


    fun setEditAction(){
        imgNewProduct.setOnClickListener {
            showProductForm()
        }
    }
}