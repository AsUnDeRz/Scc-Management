package asunder.toche.sccmanagement.contact.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
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
import android.widget.Button
import android.widget.ScrollView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ActivityEditContact
import asunder.toche.sccmanagement.contact.ComponentListener
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.contact.adapter.AddressAdapter
import asunder.toche.sccmanagement.contact.adapter.EmailAdapter
import asunder.toche.sccmanagement.contact.adapter.NumberAdapter
import asunder.toche.sccmanagement.contact.adapter.WebsiteAdapter
import asunder.toche.sccmanagement.contact.pager.ContactPager
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.TriggerContact
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.custom.extension.EnableClick
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.main.ControlViewModel
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.ROOT
import com.thefinestartist.finestwebview.FinestWebView
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_contact_add.*
import kotlinx.android.synthetic.main.section_contact_confirm.*
import kotlinx.android.synthetic.main.section_contact_info.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class ContactFragment  : Fragment(),ComponentListener{

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
    lateinit var contactVM : ContactViewModel
    private var loading = LoadingDialog.newInstance()
    private lateinit var numberAdapter : NumberAdapter
    private lateinit var emailAdapter: EmailAdapter
    private lateinit var webstieAdapter: WebsiteAdapter
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var controlViewModel: ControlViewModel
    private val companyFragment = CompanyFragment.newInstance()
    private val historyCompanyFragment = HistoryCompanyFragment.newInstance()
    private lateinit var filterFirstContact:Model.Contact




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactVM = ViewModelProviders.of(activity!!).get(ContactViewModel::class.java)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
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
            displayCompany()
            showFormContactWithData(filterFirstContact)
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
        //setUpPager()
        //setUpTablayout()
        setEditAction()
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
            //if (validateInput()) {
                saveContact()
                contactScrollView.fullScroll(ScrollView.FOCUS_UP)
            //}
        }

        btnCancelContact.setOnClickListener {
            contactVM.updateViewState(ContactState.ALLCONTACT)
            contactScrollView.fullScroll(ScrollView.FOCUS_UP)
        }

        btnDeleteContact.setOnClickListener {
            contactVM.deleteContact()
        }

        btnHistory.setOnClickListener {
            showContactList()
            displayHistory()
            triggerContact(filterFirstContact)
            //contactVM.updateViewState(ContactState.SELECTCONTACT)
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


    fun setEditAction(){
        imgEdit.setOnClickListener {
            showFormContact()
            clearModelContact()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KEY.NEW_ADDRESS -> if (resultCode == Activity.RESULT_OK && data != null){
                //update address
                addressAdapter.addAddress(data.getParcelableExtra(ROOT.ADDRESS))
                contactScrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
            KEY.EDIT_ADDRESS -> if (resultCode == Activity.RESULT_OK && data != null){
                if (data.hasExtra(KEY.DELETE)){
                    addressAdapter.remove(data.getIntExtra(ROOT.POSITION,0))
                }else{
                    addressAdapter.updateAddress(data.getParcelableExtra(ROOT.ADDRESS),
                            data.getIntExtra(ROOT.POSITION,0))
                }
            }
        }
    }

    fun saveContact(){
        val data = Model.Contact("",edtCompany.text.toString().capitalize(),edtBill.text.toString()
                ,edtContactName.text.toString(),numberAdapter.numbers,emailAdapter.emails,
                webstieAdapter.websites, addressAdapter.addresses,numberAdapter.typeList)
        contactVM.saveContact(data)
        loading.show(fragmentManager, LoadingDialog.TAG)
    }

    fun validateInput() : Boolean{
        if(TextUtils.isEmpty(edtCompany.text)){
            contactScrollView.fullScroll(ScrollView.FOCUS_UP)
            edtCompany.error = "กรุณากรอกข้อมูลบริษัท"
            return false
        }
        if(TextUtils.isEmpty(edtContactName.text)){
            contactScrollView.fullScroll(ScrollView.FOCUS_UP)
            edtContactName.error = "กรุณากรอกชื่อผู้ติดต่อ"
            return false
        }
        return true
    }

    fun showFormContactWithData(contact :Model.Contact){
        filterFirstContact = contact
        showFormContact()
        contactVM.updateContactId(contact.id)
        edtCompany.setText(contact.company)
        edtBill.setText(contact.bill)
        edtContactName.setText(contact.contact_name)
        numberAdapter.updateNumbers(contact.numbers)
        numberAdapter.updateTypeList(contact.type_number)
        emailAdapter.updateEmails(contact.email)
        emailAdapter.updateTypeList(contact.type_number)
        webstieAdapter.updateTypeList(contact.type_number)
        webstieAdapter.updateWebsites(contact.websites)
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
        imgEdit.visibility = View.GONE
    }

    fun clearModelContact(){
        val contact = Model.Contact()
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
        imgEdit.visibility = View.VISIBLE
    }

    fun checkState(){
        when (stateInput){
            CurrentInputState.Company ->{
                updateInputForm("บริษัท",edtCompany.text.toString())
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
                edtCompany.text = edtInput.text
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
                    if (loading.isShow) {
                        loading.dismiss()
                    }
                }
                ContactState.NEWCONTACT ->{

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
            }
        })
        contactVM.contacts.observe(this, Observer {
            if (contactVM.isSaveContactComplete.value == ContactState.SHOWFORM){
                it?.let {
                    if (it.isNotEmpty()){
                        filterContactForm(it.first())
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
        imgEdit.visibility = View.GONE
        contactVM.updateContactId(contact.id)
        edtCompany.setText(contact.company)
        edtBill.setText(contact.bill)
        edtContactName.setText(contact.contact_name)
        numberAdapter.updateNumbers(contact.numbers)
        numberAdapter.updateTypeList(contact.type_number)
        emailAdapter.updateEmails(contact.email)
        emailAdapter.updateTypeList(contact.type_number)
        webstieAdapter.updateTypeList(contact.type_number)
        webstieAdapter.updateWebsites(contact.websites)
        addressAdapter.updateTypeList(contact.type_number)

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
            numberAdapter.remove(position)
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
            addressAdapter.remove(position)
        }

    }

    override fun OnEmailClick(email: Model.Channel, isAction: Boolean,position:Int) {
        if(isAction){
            sendEmail(email.data)
        }else{
            emailAdapter.remove(position)
        }
    }

    override fun OnWebsiteClick(web: Model.Channel, isAction: Boolean,position:Int) {
        if(isAction){
            openWeb(web.data)
        }else{
            webstieAdapter.remove(position)
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


}