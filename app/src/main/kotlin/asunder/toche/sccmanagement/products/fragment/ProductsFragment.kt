package asunder.toche.sccmanagement.products.fragment

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ScrollView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.TriggerProduct
import asunder.toche.sccmanagement.custom.dialog.ConfirmDialog
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.SetHeight
import asunder.toche.sccmanagement.custom.extension.ShowKeyboard
import asunder.toche.sccmanagement.custom.extension.ShowScrollBar
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.main.ActivityImageViewer
import asunder.toche.sccmanagement.main.ControlViewModel
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.ComponentListener
import asunder.toche.sccmanagement.products.ProductState
import asunder.toche.sccmanagement.products.adapter.MediumRateAdapter
import asunder.toche.sccmanagement.products.adapter.ProductFileAdapter
import asunder.toche.sccmanagement.products.adapter.ProductPictureAdapter
import asunder.toche.sccmanagement.products.viewmodel.ProductViewModel
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import com.crashlytics.android.Crashlytics
import com.google.firebase.storage.FirebaseStorage
import com.snatik.storage.Storage
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import kotlinx.android.synthetic.main.fragment_product_add.*
import kotlinx.android.synthetic.main.fragment_product_history.*
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.layout_input.*
import kotlinx.android.synthetic.main.layout_medium_rate.*
import kotlinx.android.synthetic.main.section_product_confirm.*
import kotlinx.android.synthetic.main.section_product_content.*
import kotlinx.android.synthetic.main.section_product_info.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.IOException
import java.util.*

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class ProductsFragment : Fragment(),
        ConfirmDialog.ConfirmDialogListener,
        MediumRateAdapter.MediumListener,
        ComponentListener{

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
    private lateinit var transactionViewModel : TransactionViewModel
    private lateinit var controlViewModel: ControlViewModel
    private lateinit var rootLayoutInput : ScrollView
    private lateinit var rootLayoutMediumRate : ScrollView
    private lateinit var rootLayoutMediumForm : ConstraintLayout
    private lateinit var rootProductForm : ConstraintLayout
    private lateinit var mediumRateAdapter: MediumRateAdapter
    private var stateInput :CurrentInputState = CurrentInputState.PROD_NAME
    private lateinit var titleInput : TxtMedium
    private lateinit var edtInput : EdtMedium
    private var selectedDate : Date = Utils.getCurrentDate()
    private var loading = LoadingDialog.newInstance()
    private var isInitView = false
    private val productListFragment = ProductListFragment.newInstance()
    private val productHistoryFragment = ProductHistoryFragment.newInstance()
    private lateinit var pictureAdapter: ProductPictureAdapter
    private lateinit var fileAdapter: ProductFileAdapter
    private val selectedFile = arrayListOf<String>()
    private val selectedPhoto = arrayListOf<String>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productViewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        transactionViewModel = ViewModelProviders.of(activity!!).get(TransactionViewModel::class.java)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        initControllState()
    }

    fun initControllState(){
        controlViewModel.currentUI.observe(this, Observer {
            if (it == ROOT.PRODUCTS){
                if (!isInitView) {
                    initViewCreated()
                }
                displayProductList()
            }else{
                if (productViewModel.stateView.value == ProductState.SHOWFORM){
                    saveProduct()
                }
            }
        })
    }

    fun initViewCreated(){
        isInitView = true
        //setUpPager()
        //setUpTablayout()
        observProductViewModel()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_products,container,false)
        initFragment()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateStubProductAdd()
        inflateStubLayoutInput()
        inflateStubLayoutMediumRate()
    }

    fun initFragment(){
        val ft = fragmentManager?.beginTransaction()
        ft?.add(R.id.vpProduct, productHistoryFragment, ProductHistoryFragment::class.java.simpleName)
        ft?.add(R.id.vpProduct, productListFragment, ProductListFragment::class.java.simpleName)
        ft?.hide(productHistoryFragment)
        ft?.commit()

    }
    // Replace the switch method
    fun displayProductList(){
        val ft = fragmentManager?.beginTransaction()
        if (productListFragment.isAdded) { // if the fragment is already in container
            ft?.show(productListFragment)
        } else { // fragment needs to be added to frame container
            //ft?.add(R.id.rvContact, companyFragment, companyFragment::class.java.simpleName)
        }
        // Hide fragment History
        if (productHistoryFragment.isAdded) {
            ft?.hide(productHistoryFragment)
        }
        // Commit changes
        ft?.commit()
    }

    // Replace the switch method
    fun displayHistory(){
        txtBack.setOnClickListener {
            displayProductList()
            productViewModel.updateStateView(ProductState.SHOWFORMWITHPRODUCT)

        }
        val ft = fragmentManager?.beginTransaction()
        if (productHistoryFragment.isAdded) { // if the fragment is already in container
            ft?.show(productHistoryFragment)
        } else { // fragment needs to be added to frame container
            //ft?.add(R.id.rvContact, historyCompanyFragment, historyCompanyFragment::class.java.simpleName)
        }
        // Hide fragment History
        if (productListFragment.isAdded) {
            ft?.hide(productListFragment)
        }
        // Commit changes
        ft?.commit()
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
            rootLayoutMediumForm = v as ConstraintLayout
            rootLayoutMediumForm.visibility = View.GONE
            rootLayoutMediumRate = v.findViewById(R.id.priceRateScrollView)
        }
        stubMediumRate.inflate()
        initViewInStubMediumRate()
    }

    fun initViewInStubProduct(){
        initContentAdapter()
        imgNewMediumRate.setOnClickListener {
            productViewModel.updateStateView(ProductState.NEWMEDIUM)
            showMediumPriceForm()
            clearMediumPriceRate()
        }

        btnAddProduct.setOnClickListener {
            if (productViewModel.stateView.value == ProductState.TRIGGERFROMTRANSACTION){
                saveProduct()
                transactionViewModel.updateStateView(TransactionState.SHOWTRANSACTION)
                transactionViewModel.updateTransaction(transactionViewModel.transaction.value!!)
            }else {
                saveProduct()
            }
        }

        btnCancelProduct.setOnClickListener {
            if (productViewModel.stateView.value == ProductState.TRIGGERFROMTRANSACTION){
                transactionViewModel.updateStateView(TransactionState.SHOWTRANSACTION)
                transactionViewModel.updateTransaction(transactionViewModel.transaction.value!!)
            }else {
                productViewModel.updateStateView(ProductState.SHOWLIST)
                setupForm(Model.Product())
            }
        }

        btnDeleteProduct.setOnClickListener {
            productViewModel.product.value?.let {
                showConfirmDialog(it.product_name)
            }
            //productViewModel.deleteProduct()
        }


        rvMediumPrice.apply {
            mediumRateAdapter = MediumRateAdapter(mutableListOf(),this@ProductsFragment)
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = mediumRateAdapter
        }
        rvMediumPrice.ShowScrollBar()


        edtProductName.setOnClickListener {
            updateStateInput(CurrentInputState.PROD_NAME)
            showLayoutInput()
        }
        edtProductDetail.setOnClickListener {
            updateStateInput(CurrentInputState.PROD_DESC)
            showLayoutInput()
        }
        edtProductDetail.ShowScrollBar()
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

        btnHistorySell.setOnClickListener {
            productViewModel.updateStateView(ProductState.SHOWPRODUCT)
            rootProductForm.visibility = View.GONE
            rootLayoutInput.visibility = View.GONE
            rootLayoutMediumForm.visibility = View.GONE
        }

        addFile.setOnClickListener {
            selectedFile.clear()
            FilePickerBuilder.getInstance()
                    .setSelectedFiles(selectedFile)
                    .setActivityTheme(R.style.AppTheme)
                    .pickFile(this)
        }

        addPicture.setOnClickListener {
            selectedPhoto.clear()
            FilePickerBuilder.getInstance()
                    .setSelectedFiles(selectedPhoto)
                    .setActivityTheme(R.style.AppTheme)
                    .pickPhoto(this)
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
        btnCancelPrice.setOnClickListener {
            showProductForm()
        }

        btnAddPrice.setOnClickListener {
            if(validateMediumRate()) {
                addMediumPriceRate(Model.MediumRate(edtPrice.text.toString(), rdbVat.isChecked,
                        Utils.getDateStringWithDate(selectedDate), edtPriceNote.text.toString(),
                        true))
                clearMediumPriceRate()
                showProductForm()
            }else{
                rootLayoutMediumRate.fullScroll(ScrollView.FOCUS_UP)
            }
        }
        btnDeletePrice.setOnClickListener {
            deleteMediumPriceRate()
            clearMediumPriceRate()
            showProductForm()
        }

        edtPriceDate.setOnClickListener {
            //showSpinner()
            showDatePicker()
        }
    }

    fun showProductList(){
        displayProductList()
        rootProductForm.visibility = View.GONE
        rootLayoutInput.visibility = View.GONE
        rootLayoutMediumForm.visibility = View.GONE

    }
    fun showProductForm(){
        productViewModel.updateStateView(ProductState.SHOWFORM)
        productScrollView.fullScroll(ScrollView.FOCUS_UP)
        edtProductName.requestFocus()
        rootProductForm.visibility = View.VISIBLE
        rootLayoutInput.visibility = View.GONE
        rootLayoutMediumForm.visibility = View.GONE


    }
    fun showLayoutInput(){
        rootLayoutInput.visibility = View.VISIBLE
        rootProductForm.visibility = View.GONE
        rootLayoutMediumForm.visibility = View.GONE
        checkState()
        edtInput.ShowKeyboard()
    }

    fun showMediumPriceForm(){
        rootLayoutMediumRate.fullScroll(ScrollView.FOCUS_UP)
        rootLayoutInput.visibility = View.GONE
        rootProductForm.visibility = View.GONE
        rootLayoutMediumForm.visibility = View.VISIBLE
        edtPriceDate.setText(Utils.getCurrentDateShort())
        //txtValues.visibility = View.GONE
        //edtPriceValues.visibility = View.GONE
        rdbCash.visibility = View.GONE
    }

    fun setMediumPriceForm(mediumRate: Model.MediumRate){
        edtPriceDate.setText(mediumRate.date.substring(0,10))
        edtPrice.setText(mediumRate.price)
        edtPriceNote.setText(mediumRate.note)
        if (mediumRate.vat){
            radioGroup.check(rdbVat.id)
        }else{
            radioGroup.check(rdbNoVat.id)
        }
    }

    fun showDatePicker(){
        val c = Calendar.getInstance()
        c.time = selectedDate
        val mount = c.get(Calendar.MONTH)
        val dOfm = c.get(Calendar.DAY_OF_MONTH)
        val year = c.get(Calendar.YEAR)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val datePicker = android.app.DatePickerDialog(context,
                android.app.DatePickerDialog.OnDateSetListener { view, yearOf, monthOfYear, dayOfMonth ->
                    val dateSelect = Calendar.getInstance()
                    dateSelect.set(yearOf,monthOfYear,dayOfMonth,0,0,0)
                    selectedDate = dateSelect.time
                    edtPriceDate.setText(Utils.getDateStringWithDate(selectedDate).substring(0,10))
        },year,mount,dOfm)
        datePicker.show()
    }

    fun showSpinner(){
        val c = Calendar.getInstance()
        c.time = selectedDate
        val mount = c.get(Calendar.MONTH)
        val dOfm = c.get(Calendar.DAY_OF_MONTH)
        val year = c.get(Calendar.YEAR)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val spinner = SpinnerDatePickerDialogBuilder()
                .context(context)
                .callback { _, yearOf, monthOfYear, dayOfMonth, hourOf, minuteOf ->
                    val dateSelect = Calendar.getInstance()
                    dateSelect.set(yearOf,monthOfYear,dayOfMonth,hourOf,minuteOf,0)
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
        if (productViewModel.stateView.value == ProductState.SELECTMEDIUM){
            mediumRateAdapter.updateMedium(productViewModel.mediumPosition,mediumRate)
        }else {
            mediumRateAdapter.addMediumList(mediumRate)
        }
        productViewModel.updateMediumRateList(mediumRateAdapter.mediumList)
        rvMediumPrice.SetHeight(mediumRateAdapter.mediumList.size)
    }

    fun deleteMediumPriceRate(){
        if (productViewModel.stateView.value == ProductState.SELECTMEDIUM){
            mediumRateAdapter.deleteMedium(productViewModel.mediumPosition)
        }
        productViewModel.updateMediumRateList(mediumRateAdapter.mediumList)
        rvMediumPrice.SetHeight(mediumRateAdapter.mediumList.size)

    }
    fun clearMediumPriceRate(){
        edtPrice.setText("")
        edtPriceDate.setText(Utils.getCurrentDateShort())
        edtPriceNote.setText("")
        rdbVat.isChecked = true
        selectedDate = Utils.getCurrentDate()
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
        edtProductName.setText(product.product_name.lines().first())
        edtProductDetail.setText(product.product_desc)
        edtImportFrom.setText(product.import_from)
        edtPackSize.setText(product.pack_size)
        edtDesc.setText(product.desc)
        mediumRateAdapter.updateMediumList(product.medium_rate)
        productViewModel.productId = product.id
        if (product.pictures.isNotEmpty()){
            pictureAdapter.updatePictures(product.pictures)
            pictureAdapter.updateTypeList(product.types)
        }else{
            pictureAdapter.updatePictures(mutableListOf())
            pictureAdapter.updateTypeList(mutableListOf())
            selectedPhoto.clear()
        }
        if (product.files.isNotEmpty()){
            fileAdapter.updateFiles(product.files)
            fileAdapter.updateTypeList(product.types)
        }else{
            selectedFile.clear()
            fileAdapter.updateFiles(mutableListOf())
            fileAdapter.updateTypeList(mutableListOf())
        }
        rvMediumPrice.SetHeight(mediumRateAdapter.mediumList.size)

    }

    fun validateProduct():Boolean{
        if(TextUtils.isEmpty(edtProductName.text)){
            //edtProductName.error = "กรุณากรอกชื่อสินค้า"
            return false
        }
        return true
    }

    fun saveProduct(){
        if(validateProduct()){
            val product = Model.Product(productViewModel.productId,edtProductName.text.toString(),
                    edtProductDetail.text.toString(),edtImportFrom.text.toString(),
                    edtPackSize.text.toString(), edtDesc.text.toString(),
                    Utils.getDateStringWithDate(selectedDate),mediumRateAdapter.mediumList,
                    fileAdapter.typeList,pictureAdapter.pictures,fileAdapter.files)

            productViewModel.saveProduct(product)
            loading.show(fragmentManager, LoadingDialog.TAG)
        }
    }

    fun observProductViewModel(){
        productViewModel.products.observe(this, Observer {
        })

        productViewModel.mediumRate.observe(this, Observer {
        })

        productViewModel.stateView.observe(this, Observer {
            when(it){
                ProductState.NEWPRODUCT ->{
                    println("Newproduct")
                    showProductForm()
                    setupForm(Model.Product())
                }
                ProductState.SHOWLIST ->{
                    showProductList()
                    if (loading.isShow) {
                        loading.dismiss()
                    }
                }
                ProductState.SHOWFORM ->{

                }
                ProductState.SHOWINPUT ->{

                }
                ProductState.SHOWMEDIUMFORM ->{

                }
                ProductState.SHOWPRODUCT ->{
                    displayHistory()
                    //tabProduct.setScrollPosition(1,0f,true)
                    //vpProduct.currentItem = 1
                    triggerProduct(productViewModel.product.value!!)
                }
                ProductState.SHOWFORMWITHPRODUCT ->{
                    showProductForm()
                    setupForm(productViewModel.product.value!!)
                }
                ProductState.TRIGGERFROMTRANSACTION ->{
                    productScrollView.fullScroll(ScrollView.FOCUS_UP)
                    edtProductName.requestFocus()
                    rootProductForm.visibility = View.VISIBLE
                    rootLayoutInput.visibility = View.GONE
                    rootLayoutMediumForm.visibility = View.GONE
                    setupForm(productViewModel.product.value!!)
                }
            }
        })

    }

    @Subscribe
    fun triggerProduct(product: Model.Product){
        EventBus.getDefault().postSticky(TriggerProduct(product))
    }

    fun showConfirmDialog(product:String){
        val confirmDialog = ConfirmDialog.newInstance("คุณต้องการลบสินค้า $product ใช่หรือไหม","แจ้งเตือน",true)
        confirmDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        confirmDialog.customListener(this)
        confirmDialog.show(fragmentManager, ConfirmDialog::class.java.simpleName)
    }

    override fun onClickConfirm() {
        productViewModel.product.value?.let {
            productViewModel.deleteProduct(it)
        }
    }

    override fun onClickCancel() {
    }

    override fun onClickMedium(mediumRate: Model.MediumRate,position:Int) {
        productViewModel.updateMediumRate(mediumRate,position)
        showMediumPriceForm()
        setMediumPriceForm(mediumRate)
        productViewModel.updateStateView(ProductState.SELECTMEDIUM)

    }
    override fun onClickLongMedium(mediumRate: Model.MediumRate) {
        val dialog = ConfirmDialog.newInstance(mediumRate.note,"Note",false)
        dialog.customListener(this)
        dialog.show(fragmentManager,ConfirmDialog::class.java.simpleName)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> if (resultCode == Activity.RESULT_OK && data != null) {
                selectedPhoto.clear()
                selectedPhoto.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                if(selectedPhoto.size > 0) {
                    val pictureList= mutableListOf<Model.ContentForProduct>()
                    selectedPhoto.forEach {
                        val base64 = Utils.encodeImage(it, this.context!!)
                        pictureList.add(Model.ContentForProduct(local_path = it,cloud_url = base64))
                    }
                    pictureAdapter.addPictures(pictureList)
                }
            }
            FilePickerConst.REQUEST_CODE_DOC -> if (resultCode == Activity.RESULT_OK && data != null) {
                selectedFile.clear()
                selectedFile.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS))
                if(selectedFile.size > 0) {
                    val fileList = mutableListOf<Model.ContentForProduct>()
                    selectedFile.forEach {
                        fileList.add(Model.ContentForProduct(local_path = it))
                    }
                    fileAdapter.addFiles(fileList)
                }
            }
        }
    }

    override fun OnFileClick(file: Model.ContentForProduct, isDeleteOrShare: Boolean, position: Int) {
        if (isDeleteOrShare) {
            fileAdapter.remove(position)
        }else {
           openFile(file)
        }
    }



    override fun OnPictureClick(picture: Model.ContentForProduct, isDeleteOrShare: Boolean, position: Int) {
        if (isDeleteOrShare) {
            pictureAdapter.remove(position)
        }else{
            openPicture(picture)
        }
    }

    override fun updateTypeList(type: String) {
        val pictureTypeList = pictureAdapter.typeList.filter { it == type }
        if (pictureTypeList.isEmpty()){
            pictureAdapter.addType(type)
        }
        val fileTypeList = fileAdapter.typeList.filter { it == type }
        if (fileTypeList.isEmpty()){
            fileAdapter.addType(type)
        }
    }

    fun initContentAdapter(){
        pictureAdapter = ProductPictureAdapter(this@ProductsFragment)
        fileAdapter = ProductFileAdapter(this@ProductsFragment)
        rvFile.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = fileAdapter
        }
        rvPicture.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = pictureAdapter
        }
    }

    fun openFile(path: Model.ContentForProduct){
        val f = File(path.local_path)
        val fileWithinMyDir = File(path.local_path)
        if (fileWithinMyDir.exists()) {
            val intent = Intent(Intent.ACTION_VIEW)
            val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(MimeTypeMap
                            .getFileExtensionFromUrl(f.path))
            intent.setDataAndType(Uri.fromFile(f), mimeType)
            this.startActivity(Intent.createChooser(intent, "Open file with"))
        }else{
            downloadInLocalFile(path.cloud_url,true)
        }
    }

    fun openPicture(picture: Model.ContentForProduct){
        /*
        val fileWithinMyDir = File(picture.local_path)
        if (fileWithinMyDir.exists()) {
            val intent = Intent()
            intent.putExtra("path",picture.local_path)
            activity?.startActivity(intent.setClass(activity,ActivityImageViewer::class.java))
        }else{
            downloadInLocalFile(picture.cloud_url,false)
        }
        */
        val intent = Intent()
        intent.putExtra("path",picture.local_path)
        activity?.startActivity(intent.setClass(activity,ActivityImageViewer::class.java))
    }

    fun downloadInLocalFile(path: String,isFile:Boolean) {
        val stor = Storage(context)
        val externalPath = stor.externalStorageDirectory
        val newDir = externalPath + File.separator + Prefer.getUUID(this.context!!)
        stor.createDirectory(newDir)

        val storageRef = FirebaseStorage.getInstance().reference
        val content = storageRef.child(path)

        val file = File(newDir, content.name)
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val fileDownloadTask = content.getFile(file)

        loading.show(fragmentManager,LoadingDialog.TAG)
        fileDownloadTask.addOnSuccessListener {
            loading.dismiss()
            println("DownloadFileSuccess"+file.absolutePath)

            if (isFile) {
                val intent = Intent(Intent.ACTION_VIEW)
                val mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(MimeTypeMap
                                .getFileExtensionFromUrl(file.path))
                intent.setDataAndType(Uri.fromFile(file), mimeType)
                this.startActivity(Intent.createChooser(intent, "Open file with"))
            }else{
                val intent = Intent()
                intent.putExtra("path",file.path)
                activity?.startActivity(intent.setClass(activity,ActivityImageViewer::class.java))
            }
        }.addOnFailureListener { exception ->
            loading.dismiss()
            Crashlytics.log(exception.message)
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            println("onDownloadProgress $progress")
        }
    }


}