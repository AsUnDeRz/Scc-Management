package asunder.toche.sccmanagement.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.auth.ActivityLogin
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.preference.Prefer
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

/**
 *Created by ToCHe on 14/3/2018 AD.
 */
class ActivityLanding : AppCompatActivity(){


    private lateinit var  handler: Handler
    private lateinit var runnable: Runnable
    private var loading = LoadingDialog.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_landing)
        loading.show(supportFragmentManager, LoadingDialog.TAG)
        requestPermission()
        setupPreference()


    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)

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
                .withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report != null) {
                            if(!report.isAnyPermissionPermanentlyDenied) {
                                postDelayed()
                            }else{
                                finish()
                            }

                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    }
                }).check()
    }

    fun postDelayed(){
        handler = Handler()
        runnable = Runnable({})
        runnable = Runnable{

            startActivity(Intent().setClass(this, ActivityLogin::class.java))
            finish()
            loading.dismiss()
            overridePendingTransition( R.anim.fade_in, R.anim.fade_out )

        }
        handler.postDelayed(runnable,1000)

    }

    fun setupPreference(){
        val current = FirebaseAuth.getInstance().currentUser
        Prefer.saveUUID("gf08q0kR0yPQFBdW4wr3QElBL9i1",this)

    }
}