package asunder.toche.sccmanagement.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import asunder.toche.sccmanagement.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
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


/**
 *Created by ToCHe on 13/3/2018 AD.
 */
class ActivityMap : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{



    lateinit var map: GoogleMap
    lateinit var mGoogleApiClient : GoogleApiClient
    lateinit var mLocationRequest: LocationRequest
    var location: Location? = null
    private lateinit var locationCallback: LocationCallback
    var isSearch = false



    @SuppressLint("MissingPermission")
    override fun onMapReady(googlemap: GoogleMap?) {
        if(googlemap != null){
            if(isLocationEnabled(this)) {
                map = googlemap
                map.isMyLocationEnabled = true
                map.setOnMyLocationButtonClickListener(this)
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    private fun isLocationEnabled(mContext: Context): Boolean {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        if(intent.hasExtra("lat")){
            isSearch = true
            val latLng = LatLng(intent.getDoubleExtra("lat",0.0),
                    intent.getDoubleExtra("long",0.0))
            location = Location("")
            location?.latitude = latLng.latitude
            location?.longitude = latLng.longitude

        }
        setupGoogleClient()
        val mapFragment : SupportMapFragment? =
                supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    fun setupGoogleClient(){
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .build()

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){

                }
            }
        }
    }


    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onConnected(p0: Bundle?) {
        if (!isSearch) {
            enableMyLocation()
        }else{
            handleNewLocation(location)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onResume() {
        super.onResume()
        mGoogleApiClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mGoogleApiClient.stopAutoManage(this)
        mGoogleApiClient.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if(report!!.isAnyPermissionPermanentlyDenied){

                        }else{
                                val task = LocationServices.getFusedLocationProviderClient(this@ActivityMap).lastLocation
                                task.addOnCompleteListener {
                                    if(it.isComplete){
                                        location = it.result
                                        handleNewLocation(location)
                                    }
                                }
                                if (location == null) {
                                    val taskUpdate = LocationServices.
                                            getFusedLocationProviderClient(this@ActivityMap)
                                            .requestLocationUpdates(mLocationRequest,locationCallback,null)
                                    if(taskUpdate.isComplete){
                                        taskUpdate.result
                                    }
                                } else {
                                    handleNewLocation(location)
                                }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?,
                                                                    token: PermissionToken?) {
                    }
                }).check()
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

}


