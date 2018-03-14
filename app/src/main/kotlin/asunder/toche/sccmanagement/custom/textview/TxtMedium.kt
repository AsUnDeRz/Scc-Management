package asunder.toche.sccmanagement.custom.textview

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

/**
 *Created by ToCHe on 24/2/2018 AD.
 */
class TxtMedium : AppCompatTextView {

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