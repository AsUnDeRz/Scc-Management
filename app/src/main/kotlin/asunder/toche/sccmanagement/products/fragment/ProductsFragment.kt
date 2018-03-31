package asunder.toche.sccmanagement.products.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.TriggerProduct
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.ShowKeyboard
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.main.ControlViewModel
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.ProductState
import asunder.toche.sccmanagement.products.viewmodel.ProductViewModel
import asunder.toche.sccmanagement.products.adapter.MediumRateAdapter
import asunder.toche.sccmanagement.products.pager.ProductsPager
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_product_add.*
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.layout_input.*
import kotlinx.android.synthetic.main.layout_price_rate.*
import kotlinx.android.synthetic.main.section_product_confirm.*
import kotlinx.android.synthetic.main.section_product_info.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class ProductsFragment : Fragment(){

    enum class CurrentInputState{
        PROD_NAME,
        PROD_DESC,
        PROD_IMPORT,
        PROD_PACKSIZE,
        PROD_NOTE_DESC
    }
    private val TAG = this::class.java.simpleName
    companion object {
        fun newInstance(): ProductsFragment = ProductsFragment()
    }
    private lateinit var productViewModel: ProductViewModel
    private lateinit var controlViewModel: ControlViewModel

    private lateinit var rootLayoutInput : ScrollView
    private lateinit var rootLayoutMediumRate : ScrollView
    private lateinit var rootProductForm : ConstraintLayout
    private lateinit var mediumRateAdapter: MediumRateAdapter
    private var stateInput :CurrentInputState = CurrentInputState.PROD_NAME
    private lateinit var titleInput : TxtMedium
    private lateinit var edtInput : EdtMedium
    private var selectedDate : Date = Date()
    private var loading = LoadingDialog.newInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productViewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        initControllState()
    }

    fun initControllState(){
        controlViewModel.currentUI.observe(this, Observer {
            if (it == ROOT.PRODUCTS){
                initViewCreated()
            }
        })
    }

    fun initViewCreated(){
        setUpPager()
        setUpTablayout()
        setEditAction()
        observProductViewModel()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_products,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

            titleInput = v.findViewById(R.id.titleForm)
            edtInput = v.findViewById(R.id.edtInput)
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
            saveProduct()
        }

        btnCancelProduct.setOnClickListener {
            showProductList()
            setupForm(Model.Product())
        }

        btnSaveProductOnBottom.setOnClickListener {
            saveProduct()
        }

        rvMediumPrice.apply {
            mediumRateAdapter = MediumRateAdapter(mutableListOf())
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = mediumRateAdapter
        }

        edtProductName.setOnClickListener {
            updateStateInput(CurrentInputState.PROD_NAME)
            showLayoutInput()
        }
        edtProductDetail.setOnClickListener {
            updateStateInput(CurrentInputState.PROD_DESC)
            showLayoutInput()
        }
        edtImportFrom.setOnClickListener {
            updateStateInput(CurrentInputState.PROD_IMPORT)
            showLayoutInput()
        }
        edtPackSize.setOnClickListener {
            updateStateInput(CurrentInputState.PROD_PACKSIZE)
            showLayoutInput()
        }
        edtDesc.setOnClickListener {
            updateStateInput(CurrentInputState.PROD_NOTE_DESC)
            showLayoutInput()
        }

    }

    fun initViewInStubLayoutInput(){
        btnCancelInput.setOnClickListener {
            showProductForm()
            clearEdtInput()
        }
        btnSaveInput.setOnClickListener {
            updateCurrentInput()
            showProductForm()
        }
    }

    fun initViewInStubMediumRate(){
        btnCancelRate.setOnClickListener {
            showProductForm()
        }

        btnSaveRate.setOnClickListener {
            if(validateMediumRate()) {
                addMediumPriceRate(Model.MediumRate(edtPrice.text.toString(), rdbVat.isChecked,
                        edtPriceValues.text.toString(), Utils.getDateStringWithDate(selectedDate)
                        , edtPriceNote.text.toString(), true))
                clearMediumPriceRate()
                showProductForm()
            }else{
                rootLayoutMediumRate.fullScroll(ScrollView.FOCUS_UP)
            }
        }

        edtPriceDate.setOnClickListener {
            showSpinner()
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
        checkState()
        edtInput.ShowKeyboard()
    }

    fun showMediumPriceForm(){
        rootLayoutMediumRate.fullScroll(ScrollView.FOCUS_UP)
        rootLayoutInput.visibility = View.GONE
        rootProductForm.visibility = View.GONE
        rootLayoutMediumRate.visibility = View.VISIBLE
        edtPriceDate.setText(Utils.getCurrentDateShort())

    }

    fun showSpinner(){
        val c = Calendar.getInstance()
        c.time = selectedDate
        val mount = c.get(Calendar.MONTH)
        val dOfm = c.get(Calendar.DAY_OF_MONTH)
        var year = c.get(Calendar.YEAR)
        val spinner = SpinnerDatePickerDialogBuilder()
                .context(context)
                .callback { _, yearOf, monthOfYear, dayOfMonth ->
                    val dateSelect = Calendar.getInstance()
                    dateSelect.set(yearOf,monthOfYear,dayOfMonth,0,0,0)
                    selectedDate = dateSelect.time
                    edtPriceDate.setText(Utils.getDateStringWithDate(selectedDate).substring(0,10))
                }
                .spinnerTheme(R.style.DatePickerSpinner)
                .year(year)
                .monthOfYear(mount)
                .dayOfMonth(dOfm)
                .build()
        spinner.show()

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
                    productViewModel.updateProduct(Model.Product())
                }
            }
        })
    }


    fun setEditAction(){
        imgNewProduct.setOnClickListener {
            showProductForm()
            setupForm(Model.Product())
        }
    }

    fun clearEdtInput(){
        edtInput.text.clear()
    }

    fun checkState(){
        when (stateInput){
            CurrentInputState.PROD_NAME ->{
                updateInputForm("ชื่อสินค้า",edtProductName.text.toString())
            }
            CurrentInputState.PROD_DESC ->{
                updateInputForm("รายละเอียดสินค้า",edtProductDetail.text.toString())
            }
            CurrentInputState.PROD_IMPORT ->{
                updateInputForm("สินค้านำเข้าจาก",edtImportFrom.text.toString())
            }
            CurrentInputState.PROD_PACKSIZE ->{
                updateInputForm("Pack size",edtPackSize.text.toString())
            }
            CurrentInputState.PROD_NOTE_DESC ->{
                updateInputForm("รายละเอียด",edtDesc.text.toString())
            }
        }
    }

    fun updateCurrentInput(){
        when (stateInput){
            CurrentInputState.PROD_NAME ->{
                edtProductName.text = edtInput.text
            }
            CurrentInputState.PROD_DESC ->{
                edtProductDetail.text = edtInput.text
            }
            CurrentInputState.PROD_IMPORT ->{
                edtImportFrom.text = edtInput.text
            }
            CurrentInputState.PROD_PACKSIZE ->{
                edtPackSize.text = edtInput.text
            }
            CurrentInputState.PROD_NOTE_DESC ->{
                edtDesc.text = edtInput.text
            }
        }
        clearEdtInput()
    }

    fun updateInputForm(title:String,content:String){
        titleInput.text = title
        edtInput.setText(content)
    }

    fun updateStateInput(newState:CurrentInputState){
        stateInput = newState
    }

    fun addMediumPriceRate(mediumRate: Model.MediumRate){
        mediumRateAdapter.addMediumList(mediumRate)
        productViewModel.updateMediumRateList(mediumRateAdapter.mediumList)
    }

    fun clearMediumPriceRate(){
        edtPrice.setText("")
        edtPriceDate.setText("")
        edtPriceNote.setText("")
        rdbVat.isChecked = true
        edtPriceValues.setText("")
        selectedDate = Date()
    }

    fun validateMediumRate():Boolean {
        if(TextUtils.isEmpty(edtPrice.text)){
            edtPrice.error = "กรุณากรอกราคากลาง"
            return false
        }
        if(TextUtils.isEmpty(edtPriceDate.text)){
            edtPriceDate.error = "กรุณาเลือกวันที่"
            return false
        }
        return true
    }

    fun setupForm(product: Model.Product){
        edtProductName.setText(product.product_name)
        edtProductDetail.setText(product.product_desc)
        edtImportFrom.setText(product.import_from)
        edtPackSize.setText(product.pack_size)
        edtDesc.setText(product.desc)
        mediumRateAdapter.updateMediumList(product.medium_rate)
        productViewModel.productId = product.id

    }

    fun validateProduct():Boolean{
        if(TextUtils.isEmpty(edtProductName.text)){
            edtProductName.error = "กรุณากรอกชื่อสินค้า"
            return false
        }
        return true
    }

    fun saveProduct(){
        if(validateProduct()){
            val product = Model.Product(productViewModel.productId,edtProductName.text.toString(),
                    edtProductDetail.text.toString(),edtImportFrom.text.toString(),
                    edtPackSize.text.toString(), edtDesc.text.toString(),
                    Utils.getDateStringWithDate(selectedDate),mediumRateAdapter.mediumList)

            productViewModel.saveProduct(product)
            loading.show(fragmentManager, LoadingDialog.TAG)
        }
    }

    fun observProductViewModel(){
        productViewModel.products.observe(this, Observer {
        })

        productViewModel.stateView.observe(this, Observer {
            when(it){
                ProductState.SHOWLIST ->{
                    showProductList()
                    loading.dismiss()
                }
                ProductState.SHOWFORM ->{
                    showProductForm()
                    setupForm(productViewModel.product.value!!)
                }
                ProductState.SHOWINPUT ->{

                }
                ProductState.SHOWMEDIUMFORM ->{

                }
                ProductState.SHOWPRODUCT ->{
                    tabProduct.setScrollPosition(1,0f,true)
                    vpProduct.currentItem = 1
                    triggerProduct(productViewModel.product.value!!)
                }
            }
        })

    }

    @Subscribe
    fun triggerProduct(product: Model.Product){
        EventBus.getDefault().postSticky(TriggerProduct(product))
    }


}