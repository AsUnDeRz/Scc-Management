package asunder.toche.sccmanagement.preference

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceManager


/**
 *Created by ToCHe on 6/3/2018 AD.
 */
object Prefer{

    fun openFile(context:Context) : SharedPreferences{
        return SecurePreferences.Builder(context)
                .password("password")
                .filename("guard")
                .build()
    }

    fun saveUUID(uuid:String,context: Context){
        val prefer = openFile(context)
        prefer.edit().putString(KEY.UUID,uuid).apply()
    }
    fun getUUID(context: Context):String{
        val prefer = openFile(context)
        return prefer.getString(KEY.UUID,"")
    }

    fun saveSession(session: Uri, file: Uri,context: Context){
        val editor = openFile(context).edit()
        editor.putString(KEY.SESSION_UPLOAD,session.toString())
        editor.putString(KEY.FILE_PATH,file.toString())
        editor.apply()
    }
    fun getSession(context: Context) : String{
        val prefer = openFile(context)
        return prefer.getString(KEY.SESSION_UPLOAD,"")
    }
    fun getFile(context: Context) : String{
        val prefer = openFile(context)
        return prefer.getString(KEY.FILE_PATH,"")
    }

}