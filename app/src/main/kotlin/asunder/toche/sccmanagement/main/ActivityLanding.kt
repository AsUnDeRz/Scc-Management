package asunder.toche.sccmanagement.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Window
import android.view.WindowManager
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.auth.ActivityLogin
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.service.ManageUserService
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.mattcarroll.hover.overlay.OverlayPermission

/**
 *Created by ToCHe on 14/3/2018 AD.
 */
class ActivityLanding : AppCompatActivity(), ManageUserService.Sign{

    private var handler = Handler()
    private lateinit var runnable: Runnable
    private var loading = LoadingDialog.newInstance()
    lateinit var authManager : ManageUserService
    private val REQUEST_CODE_HOVER_PERMISSION = 1000


    private var mPermissionsRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_landing)
        authManager = ManageUserService()
        fetchAdmin()
        loading.show(supportFragmentManager, LoadingDialog.TAG)
        if (!mPermissionsRequested && !OverlayPermission.hasRuntimePermissionToDrawOverlay(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                OverlayPermission.createIntentToRequestOverlayPermission(this)
                startActivityForResult(OverlayPermission.createIntentToRequestOverlayPermission(this)
                        , REQUEST_CODE_HOVER_PERMISSION)
            }else{
                requestPermission()
            }
        }else{
            requestPermission()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE)
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
        runnable = Runnable({})
        runnable = Runnable{
            setupPreference()
        }
        handler.postDelayed(runnable,1000)

    }

    fun setupPreference(){
        val current = FirebaseAuth.getInstance().currentUser
        //val uid = "gf08q0kR0yPQFBdW4wr3QElBL9i1"

        val user = authManager.mAuth.currentUser
        if(user != null){
            Prefer.saveUUID(user.uid,this)
            //Prefer.saveUUID("gf08q0kR0yPQFBdW4wr3QElBL9i1",this)
            authManager.checkLogin(user.uid,this)
        }else{
            //Prefer.saveUUID("gf08q0kR0yPQFBdW4wr3QElBL9i1",this)
            //authManager.checkLogin(uid,this)
            moveToLoginActivity()
        }

    }

    override fun currentStatus(userAuth: Model.UserAuth) {
        when(userAuth.status_user){
            ROOT.APPROVE ->{
                Prefer.saveUUID(userAuth.uid,this)
                authManager.synceDatabase(userAuth.uid,object : ManageUserService.SyncData{
                    override fun syncSuccess() {
                        val intent = Intent()
                        intent.putExtra(ROOT.ADMIN,false)
                        startActivity(intent.setClass(this@ActivityLanding,ActivityMain::class.java))
                        finish()
                        loading.dismiss()
                        overridePendingTransition( R.anim.fade_in, R.anim.fade_out )
                    }
                })
            }
            ROOT.ADMIN ->{
                Prefer.saveUUID(userAuth.uid,this)
                authManager.synceDatabase(userAuth.uid,object : ManageUserService.SyncData{
                    override fun syncSuccess() {
                        val intent = Intent()
                        intent.putExtra(ROOT.ADMIN,true)
                        startActivity(intent.setClass(this@ActivityLanding,ActivityMain::class.java))
                        finish()
                        loading.dismiss()
                        overridePendingTransition( R.anim.fade_in, R.anim.fade_out )
                    }
                })
            }
            else ->{
                moveToLoginActivity()
            }
        }
    }

    fun moveToLoginActivity(){
        startActivity(Intent().setClass(this, ActivityLogin::class.java))
        finish()
        loading.dismiss()
        overridePendingTransition( R.anim.fade_in, R.anim.fade_out )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_HOVER_PERMISSION == requestCode) {
            mPermissionsRequested = true
            requestPermission()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    fun fetchAdmin(){
        authManager.checkAdmin()
    }

}