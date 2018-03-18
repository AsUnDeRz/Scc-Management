package asunder.toche.sccmanagement.contact.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.contact.pager.ContactPager
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.TriggerHistory
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.main.ActivityCamera
import asunder.toche.sccmanagement.preference.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_contact_add.*
import kotlinx.android.synthetic.main.section_contact_address.*
import kotlinx.android.synthetic.main.section_contact_confirm.*
import kotlinx.android.synthetic.main.section_contact_info.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File


/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class ContactFragment  : Fragment(),OnMapReadyCallback{


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
    private lateinit var map: GoogleMap
    private lateinit var rootInput : ScrollView
    lateinit var root : ConstraintLayout
    private lateinit var titleInput : TxtMedium
    private lateinit var edtInput : EdtMedium
    var location: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var stateInput : CurrentInputState = CurrentInputState.Company
    lateinit var contactVM : ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactVM = ViewModelProviders.of(activity!!).get(ContactViewModel::class.java)
        enableMyLocation()
        setupGoogleClient()
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
    }


    fun setUpStub1(){
        stubContactAdd.setOnInflateListener { _, v ->
            root = v as ConstraintLayout
            root.visibility = View.GONE
            Log.d(TAG,"Stub1 onInflate")


            val btnCancel = v.findViewById<Button>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                showContactList()
                contactScrollView.fullScroll(ScrollView.FOCUS_UP)
            }

            val btnInput = v.findViewById<Button>(R.id.btnInput)
            btnInput.setOnClickListener {
                if(validateInput()) saveContact()
            }

            initViewAdd(v)
        }
        stubContactAdd.inflate()
    }

    fun initViewAdd(v:View){
        val imageView = v.findViewById<ImageView>(R.id.imgMap)
        imageView.setOnClickListener {
            startActivityForResult(Intent().setClass(activity, ActivityCamera::class.java),666)
        }
        val mapFragment : SupportMapFragment? =
                fragmentManager?.findFragmentById(R.id.mapView) as? SupportMapFragment
        if(contactVM.isGranted) {
            mapFragment?.getMapAsync(this@ContactFragment)
        }

        val searchView = v.findViewById<SearchView>(R.id.searchView)
        val id = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val searchText = searchView.findViewById<TextView>(id)
        searchText.typeface = Utils.getTypeFaceMedium(v.context)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(result: String?): Boolean {
                if(result != null){
                    val latlng = result.split(",")
                    if(latlng.size >= 2) {
                        val location = Location("")
                        location.latitude = latlng[0].toDouble()
                        location.longitude = latlng[1].toDouble()
                        handleNewLocation(location)
                    }
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })

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
        edtAddress.DisableClick()
        edtAddress.setOnClickListener {
            updateState(CurrentInputState.Address)
            showFormInput()

        }

        btnSave.setOnClickListener {
            if (validateInput()) saveContact()
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
        if(requestCode == 666 && data?.getStringExtra("IMAGE") != null) {
                val path = data.getStringExtra("IMAGE")
                val image = File(path)
                contactVM.updatePathPicture(path)
            Glide.with(this@ContactFragment)
                    .load(image)
                    .into(imgMap)
            }
        }



    @SuppressLint("MissingPermission")
    override fun onMapReady(googlemap: GoogleMap?) {
        if(googlemap != null){
            map = googlemap
            map.isMyLocationEnabled = true

            val task = LocationServices.getFusedLocationProviderClient(activity!!).lastLocation
            task.addOnCompleteListener {
                if(it.isComplete){
                    location = it.result
                }
            }
            if (location == null) {
                val taskUpdate = LocationServices.
                        getFusedLocationProviderClient(activity!!)
                        .requestLocationUpdates(mLocationRequest,locationCallback,null)
                if(taskUpdate.isComplete){
                    taskUpdate.result
                }
            }
        }
    }

    fun handleNewLocation(location: Location) {
        this.location = location
        val zoomLevel = 14f
        val latLng = LatLng(location.latitude, location.longitude)
        map.clear()
        val current = MarkerOptions()
                .position(latLng)
        map.addMarker(current)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

    fun saveContact(){
        val data = Model.Contact("",edtCompany.text.toString().capitalize(),edtBill.text.toString()
                ,edtContactName.text.toString(),edtMobile.text.toString(), edtFax.text.toString(),
                edtPhone.text.toString(),edtEmail.text.toString(),edtWeb.text.toString(),
                edtTypeAddress.text.toString(),edtFactoryAddress.text.toString(),
                edtAddress.text.toString(), "", "",
                "${location?.latitude}","${location?.longitude}")


        contactVM.saveContact(data)
    }



    fun validateInput() : Boolean{
        if(TextUtils.isEmpty(edtCompany.text)){
            edtCompany.error = "กรุณากรอกข้อมูลบริษัท"
            return false
        }
        if(TextUtils.isEmpty(edtBill.text)){
            return false
        }
        if(TextUtils.isEmpty(edtContactName.text)){
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
        edtMobile.setText(contact.mobile)
        edtFax.setText(contact.fax)
        edtPhone.setText(contact.telephone)
        edtEmail.setText(contact.email)
        edtWeb.setText(contact.website)
        edtTypeAddress.setText(contact.address_type)
        edtFactoryAddress.setText(contact.address_factory)
        edtAddress.setText(contact.address)

        val image = File(contact.path_img_map)
        Glide.with(this@ContactFragment)
                .load(image)
                .into(imgMap)
        if(contact.map_latitude != "" && contact.map_longitude != "") {
            location = Location("")
            location?.latitude = contact.map_latitude.toDouble()
            location?.longitude = contact.map_longitude.toDouble()
            handleNewLocation(location!!)
        }

    }

    fun showFormContact(){
        root.visibility = View.VISIBLE
        rootInput.visibility = View.GONE
        imgEdit.visibility = View.GONE
    }

    fun clearModelContact(){
        val contact = Model.Contact()
        contactVM.updateContactId("")
        contactVM.updatePathPicture("")

        edtCompany.setText(contact.company)
        edtBill.setText(contact.bill)
        edtContactName.setText(contact.contact_name)
        edtMobile.setText(contact.mobile)
        edtFax.setText(contact.fax)
        edtPhone.setText(contact.telephone)
        edtEmail.setText(contact.email)
        edtWeb.setText(contact.website)
        edtTypeAddress.setText(contact.address_type)
        edtFactoryAddress.setText(contact.address_factory)
        edtAddress.setText(contact.address)

        Glide.with(this@ContactFragment)
                .load("")
                .into(imgMap)

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
                updateInputForm("ที่อยู่",edtAddress.text.toString())
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
                edtAddress.text = edtInput.text
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
                    }else{
                        circleState[i].isSelected = false
                        Glide.with(this@ContactFragment)
                                .load(R.drawable.ic_add_white_24dp)
                                .into(circleState[i])
                    }
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })

        }
    }


    fun setupGoogleClient(){
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    this@ContactFragment.location = location

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        contactVM.updateGranted(!report!!.isAnyPermissionPermanentlyDenied)
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?,
                                                                    token: PermissionToken?) {
                    }
                }).check()
    }



    fun observerContacts(){
        contactVM.isSaveContactComplete.observe(this, Observer {
            when(it){
                ContactState.ALLCONTACT ->{
                    showContactList()
                }
                ContactState.NEWCONTACT ->{

                }
                ContactState.EDITCONTACT ->{
                    showFormContactWithData(contactVM.contact.value!!)
                }
                ContactState.SELECTCONTACT ->{
                    tabContact.setScrollPosition(1,0f,true)
                    vpContact.currentItem = 1
                    triggerHistory(contactVM.contact.value!!)
                }
            }
        })
    }


    @Subscribe
    fun triggerHistory(contact: Model.Contact){
        EventBus.getDefault().postSticky(TriggerHistory(contact))
    }

}