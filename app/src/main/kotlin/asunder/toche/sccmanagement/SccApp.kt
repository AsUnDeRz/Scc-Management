package asunder.toche.sccmanagement

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import asunder.toche.sccmanagement.preference.SecurePreferences
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper

/**
 *Created by ToCHe on 24/2/2018 AD.
 */
class SccApp : Application(){
    override fun onCreate() {
        super.onCreate()
        SecurePreferences.init(this)
        Paper.init(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}