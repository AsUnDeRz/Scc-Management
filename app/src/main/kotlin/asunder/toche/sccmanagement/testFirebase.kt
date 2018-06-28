package asunder.toche.sccmanagement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import asunder.toche.sccmanagement.hover.HoverService
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.mattcarroll.hover.overlay.OverlayPermission
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


/**
 *Created by ToCHe on 27/2/2018 AD.
 */
class testFirebase : AppCompatActivity(){

    val TAG : String ="TESTACTIVITY"
    private val REQUEST_CODE_HOVER_PERMISSION = 1000

    private var mPermissionsRequested = false
    var MY_PERMISSIONS_REQUEST_READ_CONTACTS = 155



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        iconApp.setOnClickListener {
            HoverService.showFloatingMenu(this)
        }
        // On Android M and above we need to ask the user for permission to display the Hover
        // menu within the "alert window" layer.  Use OverlayPermission to check for the permission
        // and to request it.
        if (!mPermissionsRequested && !OverlayPermission.hasRuntimePermissionToDrawOverlay(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                OverlayPermission.createIntentToRequestOverlayPermission(this)
                startActivityForResult(OverlayPermission.createIntentToRequestOverlayPermission(this)
                        , REQUEST_CODE_HOVER_PERMISSION)
            }
        }
        requestPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_HOVER_PERMISSION == requestCode) {
            mPermissionsRequested = true
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



    fun process() = async(UI) {
        try {
            val job = async(CommonPool) {

            }
            job.await()
            //We're back on the main thread here.
            //Update UI controls such as RecyclerView adapter data.
        }
        catch (e: Exception) {
        }
        finally {
        }
    }

    fun requestPermission(){
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report != null) {
                            if(!report.isAnyPermissionPermanentlyDenied) {
                            }else{
                            }

                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    }
                }).check()
    }
}