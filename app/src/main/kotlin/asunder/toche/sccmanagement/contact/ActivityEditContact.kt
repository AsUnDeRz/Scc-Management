package asunder.toche.sccmanagement.contact

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.TriggerImage
import asunder.toche.sccmanagement.custom.extension.ShowScrollBar
import asunder.toche.sccmanagement.main.ActivityImageViewer
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ImagesService
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.text.MessageFormat
import java.util.*

/**
 *Created by ToCHe on 30/4/2018 AD.
 */
class ActivityEditContact:AppCompatActivity(), OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    @SuppressLint("MissingPermission")
    override fun onMyLocationButtonClick(): Boolean {
        val task = LocationServices.getFusedLocationProviderClient(this).lastLocation
        task.addOnCompleteListener {
            if(it.isComplete){
                location = it.result
                handleNewLocation(it.result)
            }
        }
        return true
    }

    var map: GoogleMap? = null
    var location: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var currentAddress:Model.Address
    val selectedPhoto = arrayListOf<String>()
    var currentPhoto = ""
    var base64Photo = ""
    var position = 0
    var isEditLocation:Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)
        val mapFragment : SupportMapFragment? =
                supportFragmentManager?.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(this@ActivityEditContact)

        setupGoogleClient()
        setAction()
    }

    @SuppressLint("MissingPermission")
    fun initData(contact: Model.Address){
        currentAddress = contact
        currentPhoto = contact.path_img_map
        edtTypeAddress.setText(contact.address_type)
        edtFactoryAddress.setText(contact.address_factory)
        val image = File(contact.path_img_map)
        Glide.with(this)
                .asBitmap()
                .load(image)
                .into(imgMap)
        if(contact.map_latitude != "" && contact.map_longitude != ""
                && contact.map_latitude != "null" && contact.map_longitude != "null") {
            location = Location("")
            location?.latitude = contact.map_latitude.toDouble()
            location?.longitude = contact.map_longitude.toDouble()
            handleNewLocation(location!!)
            map?.isMyLocationEnabled = false
            isEditLocation = false
        }else{
            //Toast.makeText(this,"contact.map is Empty ",Toast.LENGTH_SHORT).show()
            //13.2466906,100.3766333 13.7616035,100.5202237,5.99
            //location = Location("")
            //location?.latitude = 13.7616035
            //location?.longitude = 100.5202237
            //handleNewLocation(location!!,5.99f)
            map?.isMyLocationEnabled = true
            isEditLocation = true
        }
    }

    @SuppressLint("MissingPermission")
    fun setAction() {
        btnAddAddress.setOnClickListener {
            //val uid = UUID.randomUUID().toString()
            //ImagesService.addImage(Model.ImageScc(uid,base64Photo))
            if (intent.hasExtra(ROOT.POSITION)) {
                currentAddress = Model.Address(edtTypeAddress.text.toString(), edtFactoryAddress.text.toString(),
                        "", currentPhoto,
                        "${location?.longitude}", "${location?.latitude}")
                val result = Intent()
                result.putExtra(ROOT.ADDRESS, currentAddress)
                result.putExtra(ROOT.POSITION,intent.getIntExtra(ROOT.POSITION,0))
                setResult(Activity.RESULT_OK, result)
            } else {
                currentAddress = Model.Address(edtTypeAddress.text.toString(), edtFactoryAddress.text.toString(),
                        "", currentPhoto,
                        "${location?.longitude}", "${location?.latitude}")
                val intent = Intent()
                intent.putExtra(ROOT.ADDRESS, currentAddress)
                setResult(Activity.RESULT_OK, intent)
            }
            //triggerImage(base64Photo)
            finish()
            //ImagesService.saveToDB()
        }
        btnCancelAddress.setOnClickListener {
            finish()
        }
        btnDeleteAddress.setOnClickListener {
           // ImagesService.deleteImage(currentAddress.images_id)
            currentAddress = Model.Address(edtTypeAddress.text.toString(), edtFactoryAddress.text.toString(),
                    "", currentPhoto,
                    "${location?.longitude}", "${location?.latitude}")
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
                        location.latitude = latlng[0].trim().toDouble()
                        location.longitude = latlng[1].trim().toDouble()
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
            if(currentPhoto.isEmpty()) {
                selectedPhoto.clear()
                FilePickerBuilder.getInstance().setMaxCount(1)
                        .setSelectedFiles(selectedPhoto)
                        .setActivityTheme(R.style.AppTheme)
                        .pickPhoto(this)
            }else{
                val intent = Intent()
                intent.putExtra("path",currentPhoto)
                startActivity(intent.setClass(this@ActivityEditContact, ActivityImageViewer::class.java))
            }
        }

        imgMap.setOnLongClickListener {
            selectedPhoto.clear()
            FilePickerBuilder.getInstance().setMaxCount(1)
                    .setSelectedFiles(selectedPhoto)
                    .setActivityTheme(R.style.AppTheme)
                    .pickPhoto(this)
            return@setOnLongClickListener true
        }

        if (!intent.hasExtra(ROOT.POSITION)) {
            btnDeleteAddress.isEnabled = false
        }


        btnShare.setOnClickListener {
            val link = formatLocation(location!!, "https://maps.google.com/?q={0},{1}")

            val intent = Intent()
            intent.action = Intent.ACTION_SEND;
            intent.putExtra(Intent.EXTRA_TEXT, link)
            intent.type = "text/plain";
            startActivity(Intent.createChooser(intent, "Share location via"))
        }
        edtFactoryAddress.ShowScrollBar()

        btnOFF.setOnClickListener {
            it.setBackgroundColor(ContextCompat.getColor(this,R.color.Color_Red))
            btnOn.setBackgroundColor(ContextCompat.getColor(this, R.color.Color_White))
            if(isLocationEnabled(this)) {
                map?.isMyLocationEnabled = false
                isEditLocation = false
            }
        }
        btnOn.setOnClickListener {
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.Color_Red))
            btnOFF.setBackgroundColor(ContextCompat.getColor(this, R.color.Color_White))
            if (isLocationEnabled(this)) {
                map?.isMyLocationEnabled = true
                isEditLocation = true
            }
        }
    }

    private fun getLatitude(location: Location): String {
        return String.format(Locale.US, "%2.7f", location.latitude)
    }

    private fun getLongitude(location: Location): String {
        return String.format(Locale.US, "%3.7f", location.longitude)
    }

    private fun formatLocation(location: Location, format: String): String {
        return MessageFormat.format(format,
                getLatitude(location), getLongitude(location))
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
            if(isLocationEnabled(this)) {
                map = googlemap
                map?.isMyLocationEnabled = isEditLocation
                map?.setOnMyLocationButtonClickListener(this)
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

    private fun isLocationEnabled(mContext: Context): Boolean {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)
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
        map?.clear()
        map?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

    }

    fun handleNewLocation(location: Location,zoom:Float? = null) {
        txtLocation.text = "GPS\nLatitude ${location.latitude}\nLongitude ${location.longitude}"
        Toast.makeText(this,"OnLocationChange ${location.latitude}/${location.longitude}",Toast.LENGTH_SHORT).show()
        this.location = location
        val zoomLevel = zoom ?: 14f
        val latLng = LatLng(location.latitude, location.longitude)
        map?.clear()
        val current = MarkerOptions()
                .position(latLng)
        map?.addMarker(current)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 666 && data?.getStringExtra("IMAGE") != null) {
            val path = data.getStringExtra("IMAGE")
            val image = File(path)
            currentPhoto = path
            //base64Photo = Utils.encodeImage(path,this)
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
                    //base64Photo = Utils.encodeImage(selectedPhoto[0],this)
                    Glide.with(this)
                            .load(File(selectedPhoto[0]))
                            .into(imgMap)
                }
            }
        }
    }

    override fun onBackPressed() {

    }

    @Subscribe
    fun triggerImage(image : String){
        EventBus.getDefault().postSticky(TriggerImage(image))
    }

}