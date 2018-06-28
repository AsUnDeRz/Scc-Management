package asunder.toche.sccmanagement.preference

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceManager
import android.graphics.Bitmap
import android.graphics.Matrix


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

    fun flipIMage(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        val rotation = fixOrientation(bitmap)
        matrix.postRotate(rotation)
        matrix.preScale(-1f, 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun fixOrientation(bitmap: Bitmap): Float {
        return if (bitmap.width > bitmap.height) {
            90f
        } else 0f
    }

}