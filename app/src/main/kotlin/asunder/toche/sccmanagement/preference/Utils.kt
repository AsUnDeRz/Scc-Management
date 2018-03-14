package asunder.toche.sccmanagement.preference

import android.content.Context
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
object Utils{


    fun getCurrentDateString() : String{
            val fmtOut = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("th"))
            return fmtOut.format(getCurrentDate())
    }

    fun getDateWithString(date :String) : Date{
        val fmtOut = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("th"))
        return fmtOut.parse(date)
    }

    fun getCurrentDate():Date{
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        return calendar.time
    }

    fun getTypeFaceMedium(context : Context):Typeface {
        return Typeface.createFromAsset(context.assets, "fonts/set_medium.ttf")
    }

}