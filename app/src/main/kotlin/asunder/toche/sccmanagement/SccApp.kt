package asunder.toche.sccmanagement

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import android.support.v4.content.ContextCompat
import asunder.toche.sccmanagement.hover.theme.HoverTheme
import asunder.toche.sccmanagement.hover.theme.HoverThemeManager
import asunder.toche.sccmanagement.preference.SecurePreferences
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper
import org.greenrobot.eventbus.EventBus
import android.os.StrictMode
import android.os.Build
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import asunder.toche.sccmanagement.service.ImagesService


/**
 *Created by ToCHe on 24/2/2018 AD.
 */
class SccApp : Application(){
    override fun onCreate() {
        super.onCreate()
        SecurePreferences.init(this)
        Paper.init(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        setupTheme()
        //ImagesService.initService()
        //BigImageViewer.initialize(GlideImageLoader.with(this))

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun setupTheme() {
        val defaultTheme = HoverTheme(
                ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, R.color.colorPrimary))
        HoverThemeManager.init(EventBus.getDefault(), defaultTheme)
    }
}