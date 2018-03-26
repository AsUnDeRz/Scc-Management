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