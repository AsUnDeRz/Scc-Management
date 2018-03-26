package asunder.toche.sccmanagement.main

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import asunder.toche.sccmanagement.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.result.transformer.scaled
import io.fotoapparat.selector.back
import kotlinx.android.synthetic.main.activity_camare.*
import java.io.File
import android.app.Activity
import android.content.Intent
import android.net.Uri
import java.util.*


/**
 *Created by ToCHe on 13/3/2018 AD.
 */
class ActivityCamera : AppCompatActivity(){


    private lateinit var fotoapparat: Fotoapparat
    var isGrantedCamera : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camare)
        checkPermission()
    }


    fun checkPermission(){
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.isAnyPermissionPermanentlyDenied){
                            isGrantedCamera = false
                        }
                        else{
                            isGrantedCamera = true
                            setupCamera()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    }
                }).check()

    }

    fun setupCamera(){
        camera_view.visibility = View.VISIBLE
        capture.setOnClickListener {
            takePicture()
        }
       fotoapparat = Fotoapparat(
               context = this,
               view = camera_view,                   // view which will draw the camera preview
               scaleType = ScaleType.CenterCrop,    // (optional) we want the preview to fill the view
               lensPosition = back()               // (optional) we want back camera
       )
        fotoapparat.start()
    }

    override fun onStop() {
        fotoapparat.stop()
        super.onStop()
    }

    private fun takePicture(){
        val photoResult = fotoapparat
                .autoFocus()
                .takePicture()

        val file = File(
                getExternalFilesDir("photos"),
                "${Date().time}.jpg"
        )

        photoResult
                .saveToFile(file)
        updateImage(file)
        photoResult
                .toBitmap(scaled(scaleFactor = 0.25f))
                .whenAvailable { photo ->
                    photo
                            ?.let {
                                val resultIntent = Intent()
                                resultIntent.putExtra("IMAGE",
                                        file.path)
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                            ?: Log.e("", "Couldn't capture photo.")
                }
    }

    fun updateImage(file: File) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(file)
        sendBroadcast(intent)
    }


}