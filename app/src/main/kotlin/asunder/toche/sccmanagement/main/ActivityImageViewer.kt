package asunder.toche.sccmanagement.main

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.service.ImagesService
import au.com.jtribe.shelly.Shelly
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_image_viewer.*
import java.io.File


/**
 *Created by ToCHe on 26/5/2018 AD.
 */
class ActivityImageViewer: AppCompatActivity() {
    var storageRef = FirebaseStorage.getInstance().reference

    lateinit var imageUri:Uri
    lateinit var pathLocal:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        println(intent.getStringExtra("path"))
        //mBigImage.setProgressIndicator(ProgressPieIndicator())
        //mBigImage.setInitScaleType(BigImageView.INIT_SCALE_TYPE_CENTER_INSIDE)
        //                    .load(File(pathLocal))

        if (intent.hasExtra("path")){
            println("intent has path")
            pathLocal = intent.getStringExtra("path")
            mBigImage.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
            mBigImage.setImage(ImageSource.uri(Uri.fromFile(File(pathLocal))))
        }else{
            println("intent has url")
            storageRef.child(intent.getStringExtra("url"))
                    .downloadUrl
                    .addOnSuccessListener {
                        imageUri = it
                        //mBigImage.setImageURI(it)
                    }.addOnFailureListener {
                    }
        }
        btnShare.setOnClickListener {
            if (intent.hasExtra("path")) {
                sendShareIntent()
            }else{
                Shelly.share(this)
                        .text("Share image")
                        .video(imageUri)
                        .send()
            }
        }
        btnBack.setOnClickListener { finish() }
    }


    private fun sendShareIntent() {
        Shelly.share(this)
                .text("text with image")
                .image(Uri.fromFile(File(pathLocal)))
                .send()
    }
}