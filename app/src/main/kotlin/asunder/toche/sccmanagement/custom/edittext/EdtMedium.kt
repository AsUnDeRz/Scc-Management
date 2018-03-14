package asunder.toche.sccmanagement.custom.edittext

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.EditText

/**
 *Created by ToCHe on 25/2/2018 AD.
 */
class EdtMedium : EditText{


    constructor(context: Context) : super(context){
        applyFont(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet){
        applyFont(context)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr){
        applyFont(context)
    }

    private fun applyFont(context: Context) {
        val customFont = Typeface.createFromAsset(context.assets, "fonts/set_medium.ttf")
        typeface = customFont
    }
}