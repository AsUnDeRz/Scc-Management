package asunder.toche.sccmanagement.contact.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.pager.ContactPager
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.main.ActivityCamera
import asunder.toche.sccmanagement.preference.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.section_contact_address.*
import kotlinx.android.synthetic.main.section_contact_confirm.*
import kotlinx.android.synthetic.main.section_contact_info.*
import java.io.File


/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class ContactFragment  : Fragment(),OnMapReadyCallback{


    private val TAG = this::class.java.simpleName
    companion object {
        fun newInstance(): ContactFragment = ContactFragment()
    }

    lateinit var map: GoogleMap
    lateinit var rootInput : ConstraintLayout
    lateinit var root : ConstraintLayout
    lateinit var titleInput : TxtMedium
    lateinit var edtInput : EdtMedium
    var stateInput : CurrentInputState = CurrentInputState.Company


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
    }


    fun setUpStub1(){
        stub.setOnInflateListener { viewStub, v ->
            root = v as ConstraintLayout
            root.visibility = View.GONE
            Log.d(TAG,"Stub1 onInflate")


            val btnCancel = v.findViewById<Button>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                showContactList()
            }

            val btnInput = v.findViewById<Button>(R.id.btnInput)
            btnInput.setOnClickListener {
               showFormInput()
            }

            initViewAdd(v)
        }
        stub.inflate()
    }

    fun initViewAdd(v:View){
        val imageView = v.findViewById<ImageView>(R.id.imgMap)
        imageView.setOnClickListener {
            startActivityForResult(Intent().setClass(activity, ActivityCamera::class.java),666)
        }
        val mapFragment : SupportMapFragment? =
                fragmentManager?.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this@ContactFragment)

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

    }

    fun setUpStub2(){
        stub2.setOnInflateListener { viewStub, v ->
            rootInput = v as ConstraintLayout
            Log.d(TAG,"Stup2 onInflate")
            rootInput.visibility = View.GONE
            val btnSave = v.findViewById<Button>(R.id.btnSave)
            btnSave.setOnClickListener {
                showFormContact()
                updateCurrentInput()
            }
            val btnCancel = v.findViewById<Button>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                showFormContact()
            }

            titleInput = v.findViewById(R.id.titleForm)
            edtInput = v.findViewById(R.id.edtInput)
        }
        stub2.inflate()
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
    }

    fun setEditAction(){
        imgEdit.setOnClickListener {
            root.visibility = View.VISIBLE
            imgEdit.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 666 && data?.getStringExtra("IMAGE") != null) {
                val path = data.getStringExtra("IMAGE")
                val image = File(path)
                //val bmOptions = BitmapFactory.Options()
                //var bitmap = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
                //bitmap = Bitmap.createScaledBitmap(bitmap, imgMap.width, imgMap.height, true)
                //imgMap.setImageBitmap(bitmap)
            Log.d(TAG,"Path $path")


            Glide.with(this@ContactFragment)
                    .load(image)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(imgMap.width,imgMap.height)
                    .into(imgMap)
            }
        }



    @SuppressLint("MissingPermission")
    override fun onMapReady(googlemap: GoogleMap?) {
        if(googlemap != null){
            map = googlemap
            map.isMyLocationEnabled = true
        }
    }

    fun handleNewLocation(location: Location?) {
        val zoomLevel = 14f
        val latLng = LatLng(location!!.latitude, location.longitude)
        map.clear()
        val current = MarkerOptions()
                .position(latLng)
        map.addMarker(current)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

    fun saveContact(){


    }

    fun cancelContact(){


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

    fun showFormContact(){
        root.visibility = View.VISIBLE
        rootInput.visibility = View.GONE
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
                updateInputForm("บริษัท")
            }
            CurrentInputState.Address ->{
                updateInputForm("ที่อยู่")
            }
            CurrentInputState.Bill ->{
                updateInputForm("กำหนดวางบิล")
            }
            CurrentInputState.ContactName ->{
                updateInputForm("ข้อมูลผู้ติดต่อ")
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

    fun updateInputForm(title:String){
        titleInput.text = title
    }

    fun updateState(current : CurrentInputState) {
        stateInput = current
    }




    enum class CurrentInputState{
        Company,
        Bill,
        ContactName,
        Address
    }
}