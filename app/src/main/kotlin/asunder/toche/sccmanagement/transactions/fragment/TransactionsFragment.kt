package asunder.toche.sccmanagement.transactions.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.adapter.CompanyAdapter
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.main.ControllViewModel
import asunder.toche.sccmanagement.main.FilterViewPager
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.adapter.ProductAdapter
import asunder.toche.sccmanagement.products.viewmodel.ProductViewModel
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.adapter.SaleRateAdapter
import asunder.toche.sccmanagement.transactions.pager.TransactionPager
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.android.synthetic.main.fragment_transactions_add.*
import kotlinx.android.synthetic.main.layout_price_rate.*
import kotlinx.android.synthetic.main.section_transaction_confirm.*
import kotlinx.android.synthetic.main.section_transaction_info.*
import java.util.*

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class TransactionsFragment : Fragment(){

    private val TAG = this::class.java.simpleName
    companion object {
        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }
    private lateinit var rootLayoutPriceRate : ScrollView
    private lateinit var rootTransactionForm : ConstraintLayout
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var sheetDisableCard: BottomSheetBehavior<View>
    private lateinit var transactionVM: TransactionViewModel
    private lateinit var productVM: ProductViewModel
    private lateinit var controllViewModel: ControllViewModel
    private lateinit var saleRateAdapter: SaleRateAdapter
    private lateinit var adapter: CompanyAdapter
    private lateinit var productAdapter: ProductAdapter
    private var loading = LoadingDialog.newInstance()
    private var selectedDate = Date()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionVM = ViewModelProviders.of(activity!!).get(TransactionViewModel::class.java)
        productVM = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        controllViewModel = ViewModelProviders.of(activity!!).get(ControllViewModel::class.java)
        initControllState()
    }

    fun initControllState(){
        controllViewModel.currentUI.observe(this, Observer {
            if (it == ROOT.TRANSACTIONS){
                initViewCreated()
            }
        })
    }

    fun initViewCreated(){
        setUpPager()
        setUpTablayout()
        setEditAction()
        observerTabFilterTransactions()
        observeTransaction()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_transactions,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateStubTransactionAdd()
        inflateStubLayoutPriceRate()
    }



    fun setUpPager(){
        Log.d(TAG,"SetupPager")
       vpTransaction.adapter = TransactionPager(childFragmentManager)
       vpTransaction.setAllowedSwipeDirection(CustomViewPager.SwipeDirection.none)
       vpTransaction.offscreenPageLimit = 2


    }

    fun setUpTablayout(){
        Log.d(TAG,"SetupTablayoutr")
        tabTransaction.setCustomSize(resources.getDimensionPixelSize(R.dimen.txt20).toFloat())
        tabTransaction.setupWithViewPager(vpTransaction)
        tabTransaction.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1){
                    imgNewTransaction.visibility = View.GONE
                }else{
                    imgNewTransaction.visibility = View.VISIBLE
                }
            }
        })
    }


    fun setEditAction(){
        imgNewTransaction.setOnClickListener {
            showTransactionForm()
        }
    }


    fun observerTabFilterTransactions(){
        val viewPager = ViewPager(context!!)
        viewPager.adapter = FilterViewPager(fragmentManager,false)
        tabLayoutFilterTransaction.setupWithViewPager(viewPager)
        tabLayoutFilterTransaction.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 ->{
                        transactionVM.updateStateView(TransactionState.SORTALL)
                    }
                    1 ->{
                        transactionVM.updateStateView(TransactionState.SORTTOMORROW)
                    }
                    2 ->{
                        transactionVM.updateStateView(TransactionState.SORTYESTERDAY)
                    }
                }
            }
        })
    }

    fun inflateStubLayoutPriceRate(){
        stubPriceRate.setOnInflateListener { _, v ->
            rootLayoutPriceRate = v as ScrollView
            rootLayoutPriceRate.visibility = View.GONE
        }
        stubPriceRate.inflate()
        initViewInStubPriceRate()

    }

    fun inflateStubTransactionAdd(){
        stubTransactionAdd.setOnInflateListener { _, v ->
            rootTransactionForm = v as ConstraintLayout
            rootTransactionForm.visibility = View.GONE

        }
        stubTransactionAdd.inflate()
        initViewInStubTransactionForm()

    }

    fun initViewInStubTransactionForm(){
        btnCancelTransaction.setOnClickListener {
            showTransactionList()

        }
        btnSaveTransactionOnBottom.setOnClickListener {
            saveOrUpdateTransaction()
        }
        btnSaveTransactionOnTop.setOnClickListener {
            saveOrUpdateTransaction()
        }

        imgNewSaleRate.setOnClickListener {
            showSalePriceForm()
            txtTitlePrice.text = "ราคาลูกค้า"
        }

        edtTransactionCompany.DisableClick()
        edtTransactionProduct.DisableClick()
        edtTransactionCompany.setOnClickListener {
            showSheetCompany()
        }
        edtTransactionProduct.setOnClickListener {
            showSheetProduct()
        }

        setupRalePriceAdapter()
    }

    fun setupRalePriceAdapter(){
        rvSalePrice.apply {
            val data : MutableList<Model.SalePrice> = mutableListOf()
            for(i in 0 until 10){
                data.add(Model.SalePrice("1000$i",true,"1000$i",
                        Utils.getCurrentDateShort(),""))
            }
            data.sortByDescending { it.date }
            saleRateAdapter = SaleRateAdapter(mutableListOf())
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = saleRateAdapter
        }
    }

    fun initViewInStubPriceRate(){

        btnSaveRate.setOnClickListener {
            if(validateSaleRate()) {
                addSalePrice(Model.SalePrice(edtPrice.text.toString(),rdbVat.isChecked,
                        edtPriceValues.text.toString(),Utils.getDateStringWithDate(selectedDate),edtPriceNote.text.toString()))
                showTransactionForm()
            }
        }

        btnCancelRate.setOnClickListener {
            showTransactionForm()
        }

        edtPriceDate.setOnClickListener {
            showSpinner()
        }

    }

    fun showTransactionList(){
        rootTransactionForm.visibility = View.GONE
        rootLayoutPriceRate.visibility = View.GONE
        imgNewTransaction.visibility = View.VISIBLE
        clearTransaction()

    }
    fun showTransactionForm(){
        transactionScrollView.fullScroll(ScrollView.FOCUS_UP)
        rootTransactionForm.visibility = View.VISIBLE
        rootLayoutPriceRate.visibility = View.GONE
        imgNewTransaction.visibility = View.GONE

    }

    fun showSalePriceForm(){
        rootTransactionForm.visibility = View.GONE
        rootLayoutPriceRate.visibility = View.VISIBLE
        clearPriceForm()

    }

    fun clearPriceForm(){
        edtPriceDate.setText(Utils.getCurrentDateShort())
        edtPrice.setText("")
        edtPriceValues.setText("")
        edtPriceNote.setText("")
        rdbVat.isChecked = true

    }

    fun setupTransaction(transaction : Model.Transaction){
        Utils.findProduct(transaction.product_id,object : Utils.OnFindProductListener{
            override fun onResults(results: MutableList<Model.Product>) {
                if(results.size != 0){
                    edtTransactionProduct.setText(results[0].product_name)
                    transactionVM.updateProduct(results[0])
                }
            }
        },productVM.service.getProductsInDb())
        Utils.findCompany(transaction.company_id,object : Utils.OnFindCompanyListener{
            override fun onResults(results: MutableList<Model.Contact>) {
                if(results.size != 0){
                    edtTransactionCompany.setText(results[0].company)
                    transactionVM.updateContact(results[0])
                }
            }
        },transactionVM.contactService.getContactInDb())
        edtTransactionNote.setText(transaction.desc)
        edtMediumPrice.setText(transaction.medium_price)
        saleRateAdapter.updateSalePrice(transaction.sale_price)
        transactionVM.transactionId = transaction.id
    }

    fun clearTransaction(){
        edtTransactionCompany.setText("")
        edtTransactionProduct.setText("")
        edtTransactionNote.setText("")
        edtMediumPrice.setText("")
        saleRateAdapter.updateSalePrice(mutableListOf())
        transactionVM.transactionId = ""
        selectedDate = Date()
        transactionVM.updateProduct(Model.Product())
        transactionVM.updateContact(Model.Contact())

    }

    fun saveOrUpdateTransaction(){
        if(validateTransaction()) {
            val data = Model.Transaction(transactionVM.transactionId
                    ,transactionVM.contact.value!!.id
                    ,transactionVM.product.value!!.id
                    ,edtMediumPrice.text.toString()
                    ,Utils.getCurrentDateString()
                    ,edtTransactionNote.text.toString()
                    ,saleRateAdapter.saleList)


            transactionVM.saveTransaction(data)
            loading.show(fragmentManager, LoadingDialog.TAG)
            showTransactionList()
        }

    }

    fun validateTransaction():Boolean{
        if(TextUtils.isEmpty(edtTransactionCompany.text)){
            edtTransactionCompany.error = "กรุณาเลือกข้อมูลบริษัท"
            return false
        }
        if(TextUtils.isEmpty(edtTransactionProduct.text)){
            edtTransactionProduct.error = "กรุณาเลือกข้อมูลสินค้า"
            return false
        }
        return true
    }

    fun validateSaleRate():Boolean {
        if(TextUtils.isEmpty(edtPrice.text)){
            edtPrice.error = "กรุณากรอกราคา"
            return false
        }
        if(TextUtils.isEmpty(edtPriceDate.text)){
            edtPriceDate.error = "กรุณาเลือกวันที่"
            return false
        }
        return true
    }

    fun addSalePrice(salePrice: Model.SalePrice){
        saleRateAdapter.addSalePrice(salePrice)
        transactionVM.updateSalePriceLists(saleRateAdapter.saleList)

    }

    fun observeTransaction(){
        transactionVM.transaction.observe(this, Observer {
            if (transactionVM.stateView.value == TransactionState.SHOWTRANSACTION){
                //vpTransaction.currentItem = 1
                setupTransaction(it!!)
            }
        })
        transactionVM.stateView.observe(this, Observer {
            when(it){
                TransactionState.SHOWTRANSACTION ->{
                    showTransactionForm()
                }
                TransactionState.SHOWFORM ->{

                }
                TransactionState.SHOWINPUT ->{

                }
                TransactionState.SHOWLIST ->{
                    loading.dismiss()
                }
                TransactionState.SHOWSALEFORM ->{

                }
                TransactionState.NEWFROMCONTACT ->{
                    showTransactionForm()
                    edtTransactionCompany.setText(transactionVM.contact.value?.company)

                }
            }
        })
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

    fun setUpAdapterCompany(){
        adapter = CompanyAdapter(false)
        adapter.setUpOnClickListener(object : CompanyAdapter.CompanyOnClickListener{
            override fun onClickCompany(contact: Model.Contact) {
                edtTransactionCompany.setText(contact.company)
                transactionVM.updateContact(contact)
                bottomSheetDialog.dismiss()
            }
        })
        adapter.setContact(transactionVM.getContact())
    }

    fun setUpAdapterProduct(){
        productAdapter = ProductAdapter(productVM.service.getProductsInDb(),false)
        productAdapter.setUpOnClickListener(object : ProductAdapter.ProductOnClickListener{
            override fun onClickProduct(product: Model.Product) {
                edtTransactionProduct.setText(product.product_name)
                val price = product.medium_rate.filter { it.default }[0]
                edtMediumPrice.setText(price.price)
                transactionVM.updateProduct(product)
                bottomSheetDialog.dismiss()
            }
        })
    }

    fun showSheetCompany(){
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_company, null)
        val rvFilterCompany = bottomSheetView.findViewById<RecyclerView>(R.id.rvFilterCompany)
        val txtFilter = bottomSheetView.findViewById<EdtMedium>(R.id.txtCompanyFilter)
        bottomSheetDialog = BottomSheetDialog(context!!)
        bottomSheetDialog.setContentView(bottomSheetView)
        sheetDisableCard = BottomSheetBehavior.from(bottomSheetView.parent as View)
        if (sheetDisableCard.state != BottomSheetBehavior.STATE_EXPANDED) {
            setUpAdapterCompany()
            bottomSheetDialog.show()

        } else {
            bottomSheetDialog.dismiss()
        }
        rvFilterCompany.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = this@TransactionsFragment.adapter
        }

        txtFilter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Utils.findCompany(s.toString(),object : Utils.OnFindCompanyListener{
                    override fun onResults(results: MutableList<Model.Contact>) {
                        adapter.setContact(results)
                    }
                },transactionVM.getContact())
            }
        })

    }

    fun showSheetProduct(){
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_company, null)
        val rvFilterCompany = bottomSheetView.findViewById<RecyclerView>(R.id.rvFilterCompany)
        val txtFilter = bottomSheetView.findViewById<EdtMedium>(R.id.txtCompanyFilter)
        bottomSheetDialog = BottomSheetDialog(context!!)
        bottomSheetDialog.setContentView(bottomSheetView)
        sheetDisableCard = BottomSheetBehavior.from(bottomSheetView.parent as View)
        if (sheetDisableCard.state != BottomSheetBehavior.STATE_EXPANDED) {
            setUpAdapterProduct()
            bottomSheetDialog.show()

        } else {
            bottomSheetDialog.dismiss()
        }
        rvFilterCompany.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = this@TransactionsFragment.productAdapter
        }

        txtFilter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Utils.findProduct(s.toString(),object : Utils.OnFindProductListener{
                    override fun onResults(results: MutableList<Model.Product>) {
                        productAdapter.updateProduct(results)
                    }
                },productVM.service.getProductsInDb())

            }
        })

    }

}