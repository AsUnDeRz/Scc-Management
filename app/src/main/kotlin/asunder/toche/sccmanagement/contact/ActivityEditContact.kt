package asunder.toche.sccmanagement.contact

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import kotlinx.android.synthetic.main.activity_edit_contact.*
import java.io.File

/**
 *Created by ToCHe on 30/4/2018 AD.
 */
class ActivityEditContact:AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    var location: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var currentAddress:Model.Address
    val selectedPhoto = arrayListOf<String>()
    var currentPhoto = ""
    var position = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)
        val mapFragment : SupportMapFragment? =
                supportFragmentManager?.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(this@ActivityEditContact)
        setupGoogleClient()
        setAction()
    }

    fun initData(contact: Model.Address){
        currentAddress = contact
        edtTypeAddress.setText(contact.address_type)
        edtFactoryAddress.setText(contact.address_factory)
        val image = File(contact.path_img_map)
        Glide.with(this)
                .load(image)
                .into(imgMap)
        if(contact.map_latitude != "" && contact.map_longitude != ""
                && contact.map_latitude != "null" && contact.map_longitude != "null") {
            location = Location("")
            location?.latitude = contact.map_latitude.toDouble()
            location?.longitude = contact.map_longitude.toDouble()
            handleNewLocation(location!!)
        }
    }

    fun setAction() {
        btnAddAddress.setOnClickListener {
            if (intent.hasExtra(ROOT.POSITION)) {
                currentAddress = Model.Address(edtTypeAddress.text.toString(), edtFactoryAddress.text.toString(),
                        "", currentPhoto,
                        "${location?.longitude}", "${location?.latitude}")
                val intent = Intent()
                intent.putExtra(ROOT.ADDRESS, currentAddress)
                intent.putExtra(ROOT.POSITION,position)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                currentAddress = Model.Address(edtTypeAddress.text.toString(), edtFactoryAddress.text.toString(),
                        "", currentPhoto,
                        "${location?.latitude}", "${location?.longitude}")
                val intent = Intent()
                intent.putExtra(ROOT.ADDRESS, currentAddress)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        btnCancelAddress.setOnClickListener {
            finish()
        }
        btnDeleteAddress.setOnClickListener {
            currentAddress = Model.Address(edtTypeAddress.text.toString(), edtFactoryAddress.text.toString(),
                    "", currentPhoto,
                    "${location?.latitude}", "${location?.longitude}")
            val intent = Intent()
            intent.putExtra(ROOT.ADDRESS, position)
            intent.putExtra(KEY.DELETE,true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        val id = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val searchText = searchView.findViewById<TextView>(id)
        searchText.typeface = Utils.getTypeFaceMedium(this)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(result: String?): Boolean {
                if (result != null) {
                    val latlng = result.split(",")
                    if (latlng.size >= 2) {
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

        imgMap.setOnClickListener {
            selectedPhoto.clear()
            FilePickerBuilder.getInstance().setMaxCount(1)
                    .setSelectedFiles(selectedPhoto)
                    .setActivityTheme(R.style.AppTheme)
                    .pickPhoto(this)
        }

        if (!intent.hasExtra(ROOT.POSITION)) {
            btnDeleteAddress.isEnabled = false
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
                   this@ActivityEditContact.location = location

                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googlemap: GoogleMap?) {
        if(googlemap != null){
            map = googlemap
            map.isMyLocationEnabled = true

            val task = LocationServices.getFusedLocationProviderClient(this).lastLocation
            task.addOnCompleteListener {
                if(it.isComplete){
                    location = it.result
                }
            }
            if (location == null) {
                val taskUpdate = LocationServices.
                        getFusedLocationProviderClient(this)
                        .requestLocationUpdates(mLocationRequest,locationCallback,null)
                if(taskUpdate.isComplete){
                    taskUpdate.result
                }
            }


        }


        if (intent.hasExtra(ROOT.ADDRESS)){
            intent.getParcelableExtra<Model.Address>(ROOT.ADDRESS)?.let {
                initData(it)
            }
        }else{
            currentAddress = Model.Address()
        }
    }
    fun clearAddress(){
        currentAddress = Model.Address()
        selectedPhoto.clear()
        currentPhoto = ""
        edtTypeAddress.setText("")
        edtFactoryAddress.setText("")
        val options = RequestOptions().centerCrop()
        Glide.with(this)
                .load(R.drawable.mock_picture)
                .apply(options)
                .into(imgMap)
        val cameraPosition = CameraPosition.Builder().target(LatLng((-33).toDouble(), 150.0)).zoom(13F).build()
        map.clear()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 666 && data?.getStringExtra("IMAGE") != null) {
            val path = data.getStringExtra("IMAGE")
            val image = File(path)
            currentPhoto = path
            Glide.with(this)
                    .load(image)
                    .into(imgMap)
        }
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> if (resultCode == Activity.RESULT_OK && data != null) {
                selectedPhoto.clear()
                selectedPhoto.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                if(selectedPhoto.size > 0) {
                    currentPhoto = selectedPhoto[0]
                    Glide.with(this)
                            .load(File(selectedPhoto[0]))
                            .into(imgMap)
                }
            }
        }
    }

    override fun onBackPressed() {

    }


}