package asunder.toche.sccmanagement.contact.fragment

import android.annotation.SuppressLint
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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ActivityEditContact
import asunder.toche.sccmanagement.contact.ComponentListener
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.contact.adapter.AddressAdapter
import asunder.toche.sccmanagement.contact.adapter.EmailAdapter
import asunder.toche.sccmanagement.contact.adapter.NumberAdapter
import asunder.toche.sccmanagement.contact.adapter.WebsiteAdapter
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.TriggerContact
import asunder.toche.sccmanagement.custom.dialog.ConfirmDialog
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.custom.extension.EnableClick
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.issue.IssueState
import asunder.toche.sccmanagement.issue.IssueViewModel
import asunder.toche.sccmanagement.main.ControlViewModel
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import com.thefinestartist.finestwebview.FinestWebView
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_contact_add.*
import kotlinx.android.synthetic.main.section_contact_info.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File


/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class ContactFragment  :
        Fragment(),
        ComponentListener,
        ConfirmDialog.ConfirmDialogListener{


    private val TAG = this::class.java.simpleName
    companion object {
        fun newInstance(): ContactFragment = ContactFragment()
    }

    enum class CurrentInputState{
        Company,
        Bill,
        ContactName,
        Address
    }
    private lateinit var rootInput : ScrollView
    lateinit var root : ConstraintLayout
    private lateinit var titleInput : TxtMedium
    private lateinit var edtInput : EdtMedium
    private var stateInput : CurrentInputState = CurrentInputState.Company
    private lateinit var contactVM : ContactViewModel
    private lateinit var transactionViewModel : TransactionViewModel
    private lateinit var issueViewModel : IssueViewModel
    private var loading = LoadingDialog.newInstance()
    private lateinit var numberAdapter : NumberAdapter
    private lateinit var emailAdapter: EmailAdapter
    private lateinit var webstieAdapter: WebsiteAdapter
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var controlViewModel: ControlViewModel
    private val companyFragment = CompanyFragment.newInstance()
    private val historyCompanyFragment = HistoryCompanyFragment.newInstance()
    private lateinit var filterFirstContact:Model.Contact
    private var companyName:String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactVM = ViewModelProviders.of(activity!!).get(ContactViewModel::class.java)
        transactionViewModel = ViewModelProviders.of(activity!!).get(TransactionViewModel::class.java)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        issueViewModel = ViewModelProviders.of(activity!!).get(IssueViewModel::class.java)
        initControllState()
    }



    fun initControllState(){
        controlViewModel.currentUI.observe(this, Observer {
            if (it == ROOT.CONTACTS){
                when (contactVM.isSaveContactComplete.value){
                    ContactState.SHOWFORM ->{
                        saveContact()
                    }
                    ContactState.SELECTCONTACT ->{
                        showContactList()
                    }
                    else ->{
                        showContactList()
                    }
                }
            }else{
                if (contactVM.isSaveContactComplete.value == ContactState.SHOWFORM){
                    saveContact()
                }
            }
        })
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_contact,container,false)
        Log.d(TAG,"CreateView")
        initFragment()
        return view
    }

    fun initFragment(){
        val ft = fragmentManager?.beginTransaction()
        ft?.add(R.id.rvContact, historyCompanyFragment, historyCompanyFragment::class.java.simpleName)
        ft?.add(R.id.rvContact, companyFragment, companyFragment::class.java.simpleName)
        ft?.hide(historyCompanyFragment)
        ft?.commit()

    }
    // Replace the switch method
    fun displayCompany(){
        tabContactHistory.visibility = View.GONE
        tabContact.visibility = View.GONE
        val ft = fragmentManager?.beginTransaction()
        if (companyFragment.isAdded) { // if the fragment is already in container
            ft?.show(companyFragment)
        } else { // fragment needs to be added to frame container
            //ft?.add(R.id.rvContact, companyFragment, companyFragment::class.java.simpleName)
        }
        // Hide fragment History
        if (historyCompanyFragment.isAdded) {
            ft?.hide(historyCompanyFragment)
        }
        // Commit changes
        ft?.commit()
    }

    // Replace the switch method
    fun displayHistory(){
        tabContactHistory.visibility = View.VISIBLE
        tabContactHistory.setOnClickListener {
            if (contactVM.isSaveContactComplete.value == ContactState.SHOWFORM){
                dismissFormContact()
                contactVM.updateViewState(ContactState.SAVED)
            }else {
                contactVM.updateViewState(ContactState.ALLCONTACT)
            }
        }
        tabContact.visibility = View.VISIBLE
        tabContact.setOnClickListener {
            contactVM.updateViewState(ContactState.EDITCONTACT)
        }
        val ft = fragmentManager?.beginTransaction()
        if (historyCompanyFragment.isAdded) { // if the fragment is already in container
            ft?.show(historyCompanyFragment)
        } else { // fragment needs to be added to frame container
            //ft?.add(R.id.rvContact, historyCompanyFragment, historyCompanyFragment::class.java.simpleName)
        }
        // Hide fragment History
        if (companyFragment.isAdded) {
            ft?.hide(companyFragment)
        }
        // Commit changes
        ft?.commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated")
        setUpStub1()
        setUpStub2()
        observerContacts()
        setupAdapter()
    }

    fun setupAdapter(){
        numberAdapter = NumberAdapter(this)
        emailAdapter = EmailAdapter(this)
        webstieAdapter = WebsiteAdapter(this)
        addressAdapter = AddressAdapter(this)

        rvNumber.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = numberAdapter
        }
        rvEmail.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = emailAdapter
        }
        rvWebsite.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = webstieAdapter
        }
        rvAddress.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = addressAdapter
        }

    }

    fun setUpStub1(){
        stubContactAdd.setOnInflateListener { _, v ->
            root = v as ConstraintLayout
            root.visibility = View.GONE
            Log.d(TAG,"Stub1 onInflate")
            initViewAdd(v)
        }
        stubContactAdd.inflate()
    }

    fun initViewAdd(v:View){
        edtCompany.DisableClick()
        edtCompany.setOnClickListener {
            updateState(CurrentInputState.Company)
            showFormInput()
        }
        edtBill.DisableClick()
        edtBill.setOnClickListener {
            updateState(CurrentInputState.Bill)
            showFormInput()
        }
        edtContactName.DisableClick()
        edtContactName.setOnClickListener {
            updateState(CurrentInputState.ContactName)
            showFormInput()
        }
        imgStateAdd.setOnClickListener {
            val intent = Intent()
            startActivityForResult(intent.setClass(activity,ActivityEditContact::class.java),KEY.NEW_ADDRESS)
            //updateState(CurrentInputState.Address)
            //showFormInput()
            //updateInputForm("ที่อยู่","")
        }

        imgPhone.setOnClickListener {
            numberAdapter.addNumber(Model.Channel("",""))
        }

        imgEmail.setOnClickListener {
            emailAdapter.addEmail(Model.Channel())
        }

        imgWeb.setOnClickListener {
            webstieAdapter.addWebSite(Model.Channel())
        }

        btnAddContact.setOnClickListener {
            when {
                contactVM.isSaveContactComplete.value == ContactState.TRIGGERFROMTRANSACTION -> {
                    saveContact()
                    transactionViewModel.updateStateView(TransactionState.SHOWTRANSACTION)
                    transactionViewModel.updateTransaction(transactionViewModel.transaction.value!!)
                }
                contactVM.isSaveContactComplete.value == ContactState.TRIGGERFROMISSUE -> {
                    saveContact()
                    issueViewModel.updateViewState(IssueState.SHOWFROM)
                    issueViewModel.updateCurrentIssue(issueViewModel.currentIssue.value!!)
                }
                else -> {
                    saveContact()
                    dismissFormContact()
                    showContactList()
                    contactScrollView.fullScroll(ScrollView.FOCUS_UP)
                }
            }
        }

        btnCancelContact.setOnClickListener {
            when {
                contactVM.isSaveContactComplete.value == ContactState.TRIGGERFROMTRANSACTION -> {
                    transactionViewModel.updateStateView(TransactionState.SHOWTRANSACTION)
                    transactionViewModel.updateTransaction(transactionViewModel.transaction.value!!)
                }
                contactVM.isSaveContactComplete.value == ContactState.TRIGGERFROMISSUE ->{
                    issueViewModel.updateViewState(IssueState.SHOWFROM)
                    issueViewModel.updateCurrentIssue(issueViewModel.currentIssue.value!!)
                }
                contactVM.isSaveContactComplete.value == ContactState.SHOWFORM ->{
                    contactVM.updateViewState(ContactState.SAVED)
                    dismissFormContact()
                    contactScrollView.fullScroll(ScrollView.FOCUS_UP)
                }
                else -> {
                    showContactList()
                    dismissFormContact()
                    contactScrollView.fullScroll(ScrollView.FOCUS_UP)
                }
            }
        }

        btnDeleteContact.setOnClickListener {
            showConfirmDialog()
        }
    }

    fun setUpStub2(){
        stupLayoutInput.setOnInflateListener { viewStub, v ->
            rootInput = v as ScrollView
            Log.d(TAG,"Stup2 onInflate")
            rootInput.visibility = View.GONE
            val btnSave = v.findViewById<Button>(R.id.btnSaveInput)
            btnSave.setOnClickListener {
                showFormContact()
                updateCurrentInput()
            }
            val btnCancel = v.findViewById<Button>(R.id.btnCancelInput)
            btnCancel.setOnClickListener {
                showFormContact()
                clearEdtInput()
            }

            titleInput = v.findViewById(R.id.titleForm)
            edtInput = v.findViewById(R.id.edtInput)
        }
        stupLayoutInput.inflate()
    }


/*
    fun setUpPager(){
        Log.d(TAG,"SetupPager")
        vpContact.adapter = ContactPager(childFragmentManager)
        vpContact.setAllowedSwipeDirection(CustomViewPager.SwipeDirection.none)
        vpContact.offscreenPageLimit = 2


    }

    fun setUpTablayout(){
        Log.d(TAG,"SetupTablayoutr")
        tabContact.setCustomSize(resources.getDimensionPixelSize(R.dimen.txt20).toFloat())
        tabContact.setupWithViewPager(vpContact)
        tabContact.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1){
                    imgEdit.visibility = View.GONE
                }else{
                    imgEdit.visibility = View.VISIBLE
                    contactVM.updateContact(Model.Contact())
                }
            }
        })
    }
*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEY.NEW_ADDRESS -> if (resultCode == Activity.RESULT_OK && data != null){
                //update address
                val address = data.getParcelableExtra<Model.Address>(ROOT.ADDRESS)
                addressAdapter.addAddress(address)
                contactScrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
            KEY.EDIT_ADDRESS -> if (resultCode == Activity.RESULT_OK && data != null){
                if (data.hasExtra(KEY.DELETE)){
                    addressAdapter.remove(data.getIntExtra(ROOT.POSITION,0))
                }else{
                    val address = data.getParcelableExtra<Model.Address>(ROOT.ADDRESS)
                    addressAdapter.updateAddress(address,
                            data.getIntExtra(ROOT.POSITION,0))
                }
            }
        }
    }

    fun saveContact(){
        if (validateInput()) {
            val data = Model.Contact("", companyName?.capitalize(), edtBill.text.toString()
                    , edtContactName.text.toString(), numberAdapter.numbers, emailAdapter.emails,
                    webstieAdapter.websites, addressAdapter.addresses, numberAdapter.typeList)
            contactVM.saveContact(data)
        }
        //loading.show(fragmentManager, LoadingDialog.TAG)
    }

    fun validateInput() : Boolean{
        if(TextUtils.isEmpty(edtCompany.text)){
            contactScrollView.fullScroll(ScrollView.FOCUS_UP)
            edtCompany.error = "กรุณากรอกข้อมูลบริษัท"
            return false
        }
        /*
        if(TextUtils.isEmpty(edtContactName.text)){
            contactScrollView.fullScroll(ScrollView.FOCUS_UP)
            edtContactName.error = "กรุณากรอกชื่อผู้ติดต่อ"
            return false
        }
        */
        return true
    }

    fun showFormContactWithData(contact :Model.Contact){
        filterFirstContact = contact
        showFormContact()
        contactVM.updateContactId(contact.id)
        val lines = contact.company.lines()
        companyName = contact.company
        lines.let {
            edtCompany.setText(it[0])
        }
        edtBill.setText(contact.bill)
        edtContactName.setText(contact.contact_name)
        numberAdapter.updateNumbers(contact.numbers)
        emailAdapter.updateEmails(contact.email)
        webstieAdapter.updateWebsites(contact.websites)
        addressAdapter.updateAddress(contact.addresses)

        numberAdapter.updateTypeList(contact.type_number)
        emailAdapter.updateTypeList(contact.type_number)
        webstieAdapter.updateTypeList(contact.type_number)
        addressAdapter.updateTypeList(contact.type_number)
    }

    fun showFormContactWithOutState(contact :Model.Contact){
        filterFirstContact = contact
        contactVM.updateContactId(contact.id)
        val lines = contact.company.lines()
        companyName = contact.company
        lines.let {
            edtCompany.setText(it[0])
        }
        edtBill.setText(contact.bill)
        edtContactName.setText(contact.contact_name)
        numberAdapter.updateNumbers(contact.numbers)
        emailAdapter.updateEmails(contact.email)
        webstieAdapter.updateWebsites(contact.websites)
        addressAdapter.updateAddress(contact.addresses)

        numberAdapter.updateTypeList(contact.type_number)
        emailAdapter.updateTypeList(contact.type_number)
        webstieAdapter.updateTypeList(contact.type_number)
        addressAdapter.updateTypeList(contact.type_number)
    }

    fun showFormContact(){
        contactScrollView.fullScroll(ScrollView.FOCUS_UP)
        edtCompany.EnableClick()
        edtCompany.requestFocus()
        edtCompany.DisableClick()
        contactVM.updateViewState(ContactState.SHOWFORM)
        root.visibility = View.VISIBLE
        rootInput.visibility = View.GONE
    }

    fun dismissFormContact(){
        root.visibility = View.GONE
        rootInput.visibility = View.GONE
    }

    fun clearModelContact(){
        val contact = Model.Contact()
        companyName = ""
        contactVM.updateContactId("")
        contactVM.updatePathPicture("")
        emailAdapter.updateEmails(mutableListOf())
        addressAdapter.updateAddress(mutableListOf())
        webstieAdapter.updateWebsites(mutableListOf())
        numberAdapter.updateNumbers(mutableListOf())
        numberAdapter.updateTypeList(mutableListOf())

        edtCompany.setText(contact.company)
        edtBill.setText(contact.bill)
        edtContactName.setText(contact.contact_name)
    }

    fun showFormInput(){
        root.visibility = View.GONE
        rootInput.visibility = View.VISIBLE
        checkState()
    }

    fun showContactList(){
        displayCompany()
        root.visibility = View.GONE
    }

    fun checkState(){
        when (stateInput){
            CurrentInputState.Company ->{
                updateInputForm("บริษัท",companyName)

            }
            CurrentInputState.Address ->{
            }
            CurrentInputState.Bill ->{
                updateInputForm("กำหนดวางบิล",edtBill.text.toString())
            }
            CurrentInputState.ContactName ->{
                updateInputForm("ข้อมูลผู้ติดต่อ",edtContactName.text.toString())
            }
        }
    }

    fun updateCurrentInput(){
        when (stateInput){
            CurrentInputState.Company ->{
                val lines = edtInput.text.lines()
                companyName = edtInput.text.toString()
                lines.let {
                    edtCompany.setText(it[0])
                }
            }
            CurrentInputState.Address ->{
                //addressAdapter.addAddress(edtInput.text.toString())
            }
            CurrentInputState.Bill ->{
                edtBill.text = edtInput.text
            }
            CurrentInputState.ContactName ->{
                edtContactName.text = edtInput.text
            }
        }
        clearEdtInput()
    }
    fun clearEdtInput(){
        edtInput.text.clear()
    }

    fun updateInputForm(title:String,content:String){
        titleInput.text = title
        edtInput.setText(content)
    }

    fun updateState(current : CurrentInputState) {
        stateInput = current
    }


    fun openWeb(url:String){
        if(url.startsWith("www",true)){
            FinestWebView.Builder(activity!!).show("https://$url")
        }else {
            FinestWebView.Builder(activity!!).show(url)
        }
    }

    @SuppressLint("MissingPermission")
    fun callPhone(phone:String){
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phone")
        context?.startActivity(intent)
    }

    fun sendEmail(email:String?){
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        startActivity(Intent.createChooser(intent, "Email via..."))
    }

    fun observerContacts(){
        contactVM.isSaveContactComplete.observe(this, Observer {
            when(it){
                ContactState.ALLCONTACT ->{
                    showContactList()
                    clearModelContact()
                    if (loading.isShow) {
                        loading.dismiss()
                    }
                }
                ContactState.NEWCONTACT ->{
                    showFormContact()
                    clearModelContact()
                    triggerContact(Model.Contact())
                }
                ContactState.EDITCONTACT ->{
                    showFormContactWithData(contactVM.contact.value!!)
                }
                ContactState.SELECTCONTACT ->{
                    //tabContact.setScrollPosition(1,0f,true)
                    //vpContact.currentItem = 1
                    displayHistory()
                    triggerContact(contactVM.contact.value!!)
                }
                ContactState.TRIGGERFROMTRANSACTION ->{
                    contactScrollView.fullScroll(ScrollView.FOCUS_UP)
                    edtCompany.EnableClick()
                    edtCompany.requestFocus()
                    edtCompany.DisableClick()
                    root.visibility = View.VISIBLE
                    rootInput.visibility = View.GONE
                    showFormContactWithOutState(contactVM.contact.value!!)
                }
                ContactState.TRIGGERFROMISSUE ->{
                    contactScrollView.fullScroll(ScrollView.FOCUS_UP)
                    edtCompany.EnableClick()
                    edtCompany.requestFocus()
                    edtCompany.DisableClick()
                    root.visibility = View.VISIBLE
                    rootInput.visibility = View.GONE
                    showFormContactWithOutState(contactVM.contact.value!!)
                }
                ContactState.SAVED ->{
                    displayHistory()
                    dismissFormContact()
                }

                else -> {
                    println(it)
                }
            }
        })
        contactVM.contacts.observe(this, Observer {
            if (contactVM.isSaveContactComplete.value == ContactState.SHOWFORM
                    && controlViewModel.currentUI.value == ROOT.CONTACTS){
                it?.let { data ->
                    if (data.isNotEmpty()){
                        filterContactForm(data.first())
                    }
                }
            }
        })
    }

    fun filterContactForm(contact :Model.Contact){
        filterFirstContact = contact
        contactVM.updateViewState(ContactState.SHOWFORM)
        root.visibility = View.VISIBLE
        rootInput.visibility = View.GONE
        contactVM.updateContactId(contact.id)
        val lines = contact.company.lines()
        lines.let {
            edtCompany.setText(it[0])
        }
        edtBill.setText(contact.bill)
        edtContactName.setText(contact.contact_name)
        numberAdapter.updateNumbers(contact.numbers)
        numberAdapter.updateTypeList(contact.type_number)
        emailAdapter.updateEmails(contact.email)
        emailAdapter.updateTypeList(contact.type_number)
        webstieAdapter.updateTypeList(contact.type_number)
        webstieAdapter.updateWebsites(contact.websites)
        addressAdapter.updateTypeList(contact.type_number)
        addressAdapter.updateAddress(contact.addresses)

    }

    override fun onResume() {
        super.onResume()
        if (contactVM.isSaveContactComplete.value == ContactState.SELECTCONTACT){
            displayHistory()
            triggerContact(contactVM.contact.value!!)
        }
    }


    @Subscribe
    fun triggerContact(contact: Model.Contact){
        EventBus.getDefault().postSticky(TriggerContact(contact))
    }

    override fun OnNumberClick(number: Model.Channel, isAction: Boolean,position:Int) {
        if (isAction){
            callPhone(number.data)
        }else{
            val confirmDialog = ConfirmDialog.newInstance("คุณต้องการลบข้อมูลเบอร์โทรศัพท์ ใช่หรือไหม","แจ้งเตือน",true)
            confirmDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
            confirmDialog.customListener(object  : ConfirmDialog.ConfirmDialogListener{
                override fun onClickConfirm() {
                    numberAdapter.remove(position)
                }
                override fun onClickCancel() {
                }
            })
            confirmDialog.show(fragmentManager, ConfirmDialog::class.java.simpleName)
        }

    }

    override fun OnAddressClick(address: Model.Address, isAction: Boolean,position:Int) {
        if (isAction){
            val intent = Intent()
            intent.putExtra(ROOT.ADDRESS,address)
            intent.putExtra(ROOT.POSITION,position)
            startActivityForResult(intent.setClass(activity,ActivityEditContact::class.java),KEY.EDIT_ADDRESS)
            //updateState(CurrentInputState.Address)
            //showFormInput()
            //updateInputForm("ที่อยู่",address)
            //addressAdapter.remove(position)
        }else{
            val confirmDialog = ConfirmDialog.newInstance("คุณต้องการลบข้อมูลที่อยู่ ใช่หรือไหม","แจ้งเตือน",true)
            confirmDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
            confirmDialog.customListener(object  : ConfirmDialog.ConfirmDialogListener{
                override fun onClickConfirm() {
                    addressAdapter.remove(position)
                }
                override fun onClickCancel() {
                }
            })
            confirmDialog.show(fragmentManager, ConfirmDialog::class.java.simpleName)
        }

    }

    override fun OnEmailClick(email: Model.Channel, isAction: Boolean,position:Int) {
        if(isAction){
            sendEmail(email.data)
        }else{
            val confirmDialog = ConfirmDialog.newInstance("คุณต้องการลบข้อมูลอีเมล ใช่หรือไหม","แจ้งเตือน",true)
            confirmDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
            confirmDialog.customListener(object  : ConfirmDialog.ConfirmDialogListener{
                override fun onClickConfirm() {
                    emailAdapter.remove(position)
                }
                override fun onClickCancel() {
                }
            })
            confirmDialog.show(fragmentManager, ConfirmDialog::class.java.simpleName)
        }
    }

    override fun OnWebsiteClick(web: Model.Channel, isAction: Boolean,position:Int) {
        if(isAction){
            openWeb(web.data)
        }else{
                val confirmDialog = ConfirmDialog.newInstance("คุณต้องการลบข้อมูลเว็ปไซด์ ใช่หรือไหม","แจ้งเตือน",true)
                confirmDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
                confirmDialog.customListener(object  : ConfirmDialog.ConfirmDialogListener{
                    override fun onClickConfirm() {
                        webstieAdapter.remove(position)
                    }
                    override fun onClickCancel() {
                    }
                })
                confirmDialog.show(fragmentManager, ConfirmDialog::class.java.simpleName)
        }
    }

    override fun updateTypeList(type: String) {
        val numberTypeList = numberAdapter.typeList.filter { it == type }
        if (numberTypeList.isEmpty()){
            numberAdapter.addType(type)
        }
        val emailTypeList = emailAdapter.typeList.filter { it == type }
        if (emailTypeList.isEmpty()){
            emailAdapter.addType(type)
        }
        val webTypeList = webstieAdapter.typeList.filter { it == type }
        if (webTypeList.isEmpty()){
            webstieAdapter.addType(type)
        }
        val addressList = addressAdapter.typeList.filter { it == type }
        if (addressList.isEmpty()){
            addressAdapter.addType(type)
        }
    }

    fun showConfirmDialog(){
        val contact = contactVM.contact.value
        val confirmDialog = ConfirmDialog.newInstance("คุณต้องการลบข้อมูลการติดต่อ ประเด็น และการซื้อขายสินค้า ของ ${contact?.company} ใช่หรือไหม","แจ้งเตือน",true)
        confirmDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        confirmDialog.customListener(this)
        confirmDialog.show(fragmentManager, ConfirmDialog::class.java.simpleName)
    }

    override fun onClickConfirm() {
        contactVM.deleteContact()
        contactVM.contact.value?.let {
            issueViewModel.service.deleteIssueWithContact(it)
            transactionViewModel.service.deleteTransactionWithContact(it)
        }


    }

    override fun onClickCancel() {
    }



}