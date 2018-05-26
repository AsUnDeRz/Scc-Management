package asunder.toche.sccmanagement.transactions.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
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
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.contact.adapter.CompanyAdapter
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.custom.extension.EnableClick
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.main.ControlViewModel
import asunder.toche.sccmanagement.main.FilterViewPager
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.ProductState
import asunder.toche.sccmanagement.products.adapter.ProductAdapter
import asunder.toche.sccmanagement.products.viewmodel.ProductViewModel
import asunder.toche.sccmanagement.transactions.ActivityHistory
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.adapter.SaleRateAdapter
import asunder.toche.sccmanagement.transactions.pager.TransactionPager
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.android.synthetic.main.fragment_transactions_add.*
import kotlinx.android.synthetic.main.layout_price_rate.*
import kotlinx.android.synthetic.main.section_transaction_info.*
import java.util.*

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class TransactionsFragment : Fragment(), SaleRateAdapter.SaleRateListener {


    private val TAG = this::class.java.simpleName
    companion object {
        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }
    private lateinit var rootLayoutPriceForm : ConstraintLayout
    private lateinit var rootLayoutPriceRate : ScrollView
    private lateinit var rootTransactionForm : ConstraintLayout
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var sheetDisableCard: BottomSheetBehavior<View>
    private lateinit var transactionVM: TransactionViewModel
    private lateinit var productVM: ProductViewModel
    private lateinit var controlViewModel: ControlViewModel
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var saleRateAdapter: SaleRateAdapter
    private lateinit var adapter: CompanyAdapter
    private lateinit var productAdapter: ProductAdapter
    private var loading = LoadingDialog.newInstance()
    private var selectedDate = Utils.getCurrentDate()
    private var isInitView = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionVM = ViewModelProviders.of(activity!!).get(TransactionViewModel::class.java)
        productVM = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        contactViewModel = ViewModelProviders.of(activity!!).get(ContactViewModel::class.java)
        initControllState()
    }

    fun initControllState(){
        controlViewModel.currentUI.observe(this, Observer {
            if (it == ROOT.TRANSACTIONS){
                if (!isInitView){
                    initViewCreated()
                }

            }else{
                if (transactionVM.stateView.value == TransactionState.SHOWFORM){
                    saveOrUpdateTransaction()
                }
            }
        })
    }

    fun initViewCreated(){
        isInitView = true
        setUpPager()
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
       //vpTransaction.offscreenPageLimit = 2


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
                        println("Tabl filter transaction select")
                        transactionVM.updateStateView(TransactionState.SORTALL)
                    }
                    1 ->{
                        println("Tabl filter transaction select")
                        transactionVM.updateStateView(TransactionState.SORTTOMORROW)
                    }
                    2 ->{
                        println("Tabl filter transaction select")
                        transactionVM.updateStateView(TransactionState.SORTYESTERDAY)
                    }
                }
            }
        })
    }

    fun inflateStubLayoutPriceRate(){
        stubPriceRate.setOnInflateListener { _, v ->
            rootLayoutPriceForm = v as ConstraintLayout
            rootLayoutPriceForm.visibility = View.GONE
            rootLayoutPriceRate = v.findViewById(R.id.priceRateScrollView)
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

        openContact.setOnClickListener {
            contactViewModel.updateContact(transactionVM.contact.value!!)
            contactViewModel.updateViewState(ContactState.TRIGGERFROMTRANSACTION)

        }

        openProduct.setOnClickListener {
            productVM.updateProduct(transactionVM.product.value!!)
            productVM.updateStateView(ProductState.TRIGGERFROMTRANSACTION)
        }

    }

    fun initViewInStubTransactionForm(){
        btnCancelTransaction.setOnClickListener {
            if (transactionVM.stateView.value == TransactionState.NEWFROMCONTACT){
                contactViewModel.updateViewState(ContactState.SELECTCONTACT)
                transactionVM.updateStateView(TransactionState.SHOWLIST)
            }else {
                showTransactionList()
            }
        }

        btnDeleteTransaction.setOnClickListener {
            transactionVM.deleteTransaction()
        }

        btnAddTransaction.setOnClickListener {
            if (transactionVM.stateView.value == TransactionState.NEWFROMCONTACT){
                saveOrUpdateTransaction()
                contactViewModel.updateViewState(ContactState.SELECTCONTACT)
                transactionVM.updateStateView(TransactionState.SHOWLIST)
            }else {
                saveOrUpdateTransaction()
            }
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

        btnOpenHistory.setOnClickListener {
            val intent = Intent()
            intent.putExtra(ROOT.PRODUCTS,transactionVM.product.value?.id)
            startActivity(intent.setClass(activity,ActivityHistory::class.java))
        }
    }

    fun setupRalePriceAdapter(){
        rvSalePrice.apply {
            saleRateAdapter = SaleRateAdapter(mutableListOf(),this@TransactionsFragment)
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = saleRateAdapter
        }
    }

    fun getSaleType():String{
        return when(radioGroup.checkedRadioButtonId){
            rdbNoVat.id ->{
                ROOT.NOVAT
            }
            rdbVat.id ->{
                ROOT.VAT
            }
            rdbCash.id ->{
                ROOT.CASH
            }
            else ->{
                ""
            }
        }
    }

    fun initViewInStubPriceRate(){
        btnAddPrice.setOnClickListener {
            if(validateSaleRate()) {
                addSaleRate(Model.SalePrice(edtPrice.text.toString(), getSaleType(),
                        edtPriceValues.text.toString(), Utils.getDateStringWithDate(selectedDate), edtPriceNote.text.toString()))
                clearPriceForm()
                showTransactionForm()
            }else{

            }
        }
        btnDeletePrice.setOnClickListener {
            deleteSaleRate()
            clearSaleRate()
            showTransactionForm()
        }

        btnCancelPrice.setOnClickListener {
            showTransactionForm()
        }

        edtPriceDate.setOnClickListener {
            showSpinner()
        }

    }

    fun showTransactionList(){
        rootTransactionForm.visibility = View.GONE
        rootLayoutPriceForm.visibility = View.GONE
        clearTransaction()
        transactionScrollView.fullScroll(ScrollView.FOCUS_UP)
    }
    fun showTransactionForm(){
        edtTransactionNote.clearFocus()
        transactionVM.updateStateView(TransactionState.SHOWFORM)
        edtTransactionCompany.EnableClick()
        edtTransactionCompany.requestFocus()
        edtTransactionCompany.DisableClick()
        transactionScrollView.fullScroll(ScrollView.FOCUS_UP)
        rootTransactionForm.visibility = View.VISIBLE
        rootLayoutPriceForm.visibility = View.GONE
    }

    fun showSalePriceForm(){
        rootLayoutPriceRate.fullScroll(ScrollView.FOCUS_UP)
        rootTransactionForm.visibility = View.GONE
        rootLayoutPriceForm.visibility = View.VISIBLE
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
        selectedDate = Utils.getCurrentDate()
        transactionVM.updateProduct(Model.Product())
        transactionVM.updateContact(Model.Contact())

    }

    fun saveOrUpdateTransaction(){
        if(validateTransaction()) {
            val data = Model.Transaction(transactionVM.transactionId
                    ,transactionVM.contact.value!!.id
                    ,transactionVM.contact.value!!.company+"#"+transactionVM.contact.value!!.contact_name
                    ,transactionVM.product.value!!.id
                    ,transactionVM.product.value!!.product_name+"#"+transactionVM.product.value!!.product_desc
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
            println("OnTransaction values change ${transactionVM.stateView.value}")
            if (transactionVM.stateView.value == TransactionState.SHOWTRANSACTION){
                //vpTransaction.currentItem = 1
                setupTransaction(it!!)
                println("OnTransaction setup with showTransaction")
            }
            if (transactionVM.stateView.value == TransactionState.SHOWFORM){
                //vpTransaction.currentItem = 1
                setupTransaction(it!!)
                println("OnTransaction setup with showfrom")
            }
        })
        transactionVM.stateView.observe(this, Observer {
            when(it){
                TransactionState.NEWTRANSACTION ->{
                    println("New transaction")
                    showTransactionForm()
                }
                TransactionState.SHOWTRANSACTION ->{
                    showTransactionForm()
                }
                TransactionState.SHOWFORM ->{

                }
                TransactionState.SHOWINPUT ->{

                }
                TransactionState.SHOWLIST ->{
                    showTransactionList()
                    if (loading.isShow) {
                        loading.dismiss()
                    }
                }
                TransactionState.SHOWSALEFORM ->{

                }
                TransactionState.NEWFROMCONTACT ->{
                    edtTransactionCompany.setText("")
                    edtTransactionProduct.setText("")
                    edtTransactionNote.setText("")
                    edtMediumPrice.setText("")
                    saleRateAdapter.updateSalePrice(mutableListOf())
                    transactionVM.transactionId = ""
                    selectedDate = Utils.getCurrentDate()
                    transactionVM.updateProduct(Model.Product())
                    edtTransactionNote.clearFocus()
                    edtTransactionCompany.EnableClick()
                    edtTransactionCompany.requestFocus()
                    edtTransactionCompany.DisableClick()
                    transactionScrollView.fullScroll(ScrollView.FOCUS_UP)
                    rootTransactionForm.visibility = View.VISIBLE
                    rootLayoutPriceForm.visibility = View.GONE
                    edtTransactionCompany.setText(transactionVM.contact.value?.company)

                }
                TransactionState.SORTALL ->{
                    println(it)
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
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val spinner = SpinnerDatePickerDialogBuilder()
                .context(context)
                .callback { _, yearOf, monthOfYear, dayOfMonth, hourOf, minuteOf ->
                    val dateSelect = Calendar.getInstance()
                    dateSelect.set(yearOf,monthOfYear,dayOfMonth,hour,minute,0)
                    selectedDate = dateSelect.time
                    edtPriceDate.setText(Utils.getDateStringWithDate(selectedDate).substring(0,10))
                }
                .spinnerTheme(R.style.DatePickerSpinner)
                .year(year)
                .monthOfYear(mount)
                .dayOfMonth(dOfm)
                .hour(hour)
                .minute(minute)
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
        val products = productVM.service.getProductsInDb()
        products.sortBy { it.product_name }
        productAdapter = ProductAdapter(products,false)
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

    fun setSaleRateForm(salePrice: Model.SalePrice){
        /*
        txtSaleDate?.text = Utils.format2DigiYMD(salePrice.date)
            txtSalePrice?.text = salePrice.price
            txtSaleValues?.text = salePrice.values
            txtSaleVat?.text = getSaleType(salePrice.vat)
         */
        edtPriceNote.setText(salePrice.note)
        edtPriceDate.setText(Utils.format2DigiYMD(salePrice.date))
        edtPrice.setText(salePrice.price)
        edtPriceValues.setText(salePrice.values)
        getSaleType(salePrice.vat)
    }

    fun getSaleType(typePrice:String){
        when(typePrice){
            ROOT.NOVAT->{
                radioGroup.check(rdbNoVat.id)
            }
            ROOT.VAT ->{
                radioGroup.check(rdbVat.id)
            }
            ROOT.CASH->{
                radioGroup.check(rdbCash.id)
            }
            else ->{
            }
        }
    }

    fun addSaleRate(salePrice: Model.SalePrice){
        if (transactionVM.stateView.value == TransactionState.SELECTSALEPRICE){
            saleRateAdapter.updateSaleRate(transactionVM.salePricePosition,salePrice)
        }else {
            saleRateAdapter.addSaleList(salePrice)
        }
        transactionVM.updateSalePriceLists(saleRateAdapter.saleList)
    }

    fun deleteSaleRate(){
        if (transactionVM.stateView.value == TransactionState.SELECTSALEPRICE){
            saleRateAdapter.deleteSaleRate(transactionVM.salePricePosition)
        }
        transactionVM.updateSalePriceLists(saleRateAdapter.saleList)
    }

    fun clearSaleRate(){
        edtPrice.setText("")
        edtPriceDate.setText(Utils.getCurrentDateShort())
        edtPriceNote.setText("")
        rdbVat.isChecked = true
        edtPriceValues.setText("")
        selectedDate = Utils.getCurrentDate()
    }

    override fun onClickSaleRate(salePrice: Model.SalePrice, position: Int) {
        transactionVM.updateSalePrice(salePrice,position)
        showSalePriceForm()
        setSaleRateForm(salePrice)
        transactionVM.updateStateView(TransactionState.SELECTSALEPRICE)

    }

    override fun onClickLongSaleRate(salePrice: Model.SalePrice) {
    }

}