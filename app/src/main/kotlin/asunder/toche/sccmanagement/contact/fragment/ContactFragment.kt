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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactVM = ViewModelProviders.of(activity!!).get(ContactViewModel::class.java)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        initControllState()
    }

    fun initControllState(){
        controlViewModel.currentUI.observe(this, Observer {
            if (it == ROOT.CONTACTS){
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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated")
        setUpPager()
        setUpTablayout()
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


            val btnCancel = v.findViewById<Button>(R.id.btnCancelContact)
            btnCancel.setOnClickListener {
                showContactList()
                contactScrollView.fullScroll(ScrollView.FOCUS_UP)
            }

            val btnInput = v.findViewById<Button>(R.id.btnAddContact)
            btnInput.setOnClickListener {
                //if(validateInput()) {
                    saveContact()
                    contactScrollView.fullScroll(ScrollView.FOCUS_UP)
                //}
            }
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
            numberAdapter.addNumber(Model.Number("",""))
        }

        imgEmail.setOnClickListener {
            emailAdapter.addEmail("")
        }

        imgWeb.setOnClickListener {
            webstieAdapter.addWebSite("")
        }

        btnAddContact.setOnClickListener {
            //if (validateInput()) {
                saveContact()
                contactScrollView.fullScroll(ScrollView.FOCUS_UP)
            //}
        }

        btnDeleteContact.setOnClickListener {
            contactVM.deleteContact()
        }

        observeStateInput()
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
        showFormContact()
        contactVM.updateContactId(contact.id)
        edtCompany.setText(contact.company)
        edtBill.setText(contact.bill)
        edtContactName.setText(contact.contact_name)
        numberAdapter.updateNumbers(contact.numbers)
        numberAdapter.updateTypeList(contact.type_number)
        emailAdapter.updateEmails(contact.email)
        webstieAdapter.updateWebsites(contact.websites)
        /*
        addressAdapter.updateAddress(contact.addresses)
        edtTypeAddress.setText(contact.address_type)
        edtFactoryAddress.setText(contact.address_factory)

        val image = File(contact.path_img_map)
        Glide.with(this@ContactFragment)
                .load(image)
                .into(imgMap)
        if(contact.map_latitude != "" && contact.map_longitude != ""
                && contact.map_latitude != "null" && contact.map_longitude != "null") {
            location = Location("")
            location?.latitude = contact.map_latitude.toDouble()
            location?.longitude = contact.map_longitude.toDouble()
            handleNewLocation(location!!)
        }
        */

    }

    fun showFormContact(){
        contactScrollView.fullScroll(ScrollView.FOCUS_UP)
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
        //edtTypeAddress.setText(contact.address_type)
        //edtFactoryAddress.setText(contact.address_factory)

        /*
        val options = RequestOptions().centerCrop()
        Glide.with(this@ContactFragment)
                .load(R.drawable.mock_picture)
                .apply(options)
                .into(imgMap)
        val cameraPosition = CameraPosition.Builder().target(LatLng((-33).toDouble(), 150.0)).zoom(13F).build()
        map.clear()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        */


    }

    fun showFormInput(){
        root.visibility = View.GONE
        rootInput.visibility = View.VISIBLE
        checkState()
    }

    fun showContactList(){
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

    fun observeStateInput(){
        /*
        val actionState = arrayListOf(actionMobile,null,actionPhone,actionEmail,actionWebsite,null)
        val circleState = arrayListOf(imgCircleMobile,imgFax,imgPhone,imgEmail,imgWeb,imgStateAdd)
        val edtObserver = arrayListOf(edtMobile,edtFax,edtPhone,edtEmail,edtWeb,edtAddress)
        for (i in 0 until edtObserver.size){
            edtObserver[i].addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(text: Editable?) {
                    if(!text.isNullOrEmpty()) {
                        circleState[i].isSelected = true
                        Glide.with(this@ContactFragment)
                                .load(R.drawable.ic_remove_white_24dp)
                                .into(circleState[i])
                        actionState[i]?.let {
                            it.visibility = View.VISIBLE
                        }
                    }else{
                        circleState[i].isSelected = false
                        Glide.with(this@ContactFragment)
                                .load(R.drawable.ic_add_white_24dp)
                                .into(circleState[i])
                        actionState[i]?.let {
                            it.visibility = View.GONE
                        }
                    }
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })

        }
        setAction()
        */
    }

    fun setAction(){
        /*
        actionMobile.setOnClickListener {
            callPhone(edtMobile.text.toString())

        }
        actionPhone.setOnClickListener {
            callPhone(edtPhone.text.toString())
        }
        actionEmail.setOnClickListener {
            sendEmail(edtEmail.text.toString())

        }
        actionWebsite.setOnClickListener {
            openWeb(edtWeb.text.toString())

        }
        */

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
                    tabContact.setScrollPosition(1,0f,true)
                    vpContact.currentItem = 1
                    triggerContact(contactVM.contact.value!!)
                }
            }
        })
    }


    @Subscribe
    fun triggerContact(contact: Model.Contact){
        EventBus.getDefault().postSticky(TriggerContact(contact))
    }

    override fun OnNumberClick(number: Model.Number, isAction: Boolean,position:Int) {
        if (isAction){
            callPhone(number.number)
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

    override fun OnEmailClick(email: String, isAction: Boolean,position:Int) {
        if(isAction){
            sendEmail(email)
        }else{
            emailAdapter.remove(position)
        }
    }

    override fun OnWebsiteClick(web: String, isAction: Boolean,position:Int) {
        if(isAction){
            openWeb(web)
        }else{
            webstieAdapter.remove(position)
        }
    }


}