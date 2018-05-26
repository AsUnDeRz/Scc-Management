package asunder.toche.sccmanagement.main

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import asunder.toche.sccmanagement.R
import au.com.jtribe.shelly.Shelly
import kotlinx.android.synthetic.main.activity_image_viewer.*
import java.io.File


/**
 *Created by ToCHe on 26/5/2018 AD.
 */
class ActivityImageViewer: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        println(intent.getStringExtra("path"))
        mBigImage.showImage(Uri.fromFile(File(intent.getStringExtra("path"))))
        btnShare.setOnClickListener {
            sendShareIntent("")
        }
        btnBack.setOnClickListener { finish() }
    }


    private fun sendShareIntent(sharingText:String) {
        Shelly.share(this)
                .text("text with image")
                .image(Uri.fromFile(File(intent.getStringExtra("path"))))
                .send()
    }



}