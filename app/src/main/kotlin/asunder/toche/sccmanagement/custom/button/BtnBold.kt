package asunder.toche.sccmanagement.custom.button

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.Button
import asunder.toche.sccmanagement.R

/**
 *Created by ToCHe on 25/2/2018 AD.
 */
class BtnBold : Button {

    constructor(context: Context) : this(context,null)

    constructor(context: Context, attributeSet: AttributeSet?) :this(context,attributeSet, R.attr.buttonStyle)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
            super(context, attributeSet, defStyleAttr){
        super.setTypeface(Typeface.createFromAsset(context.assets, "fonts/set_bold.ttf"))
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupButton() {
        val res = resources
        super.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context,  R.drawable.icon_fb_white), null, null, null)
        //super.setCompoundDrawablePadding(
        // res.getDimensionPixelSize(R.dimen.login_btn_drawable_padding))
        super.setTextColor(ContextCompat.getColor(context,android.R.color.white))
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimensionPixelSize(R.dimen.login_btn_text_size).toFloat())
        super.setTypeface(Typeface.createFromAsset(context.assets, "fonts/set_bold.ttf"))
        super.setPadding(res.getDimensionPixelSize(R.dimen.login_btn_left_padding), 0,
                res.getDimensionPixelSize(R.dimen.login_btn_right_padding), 0)
        super.setBackgroundResource(R.drawable.btn_fb)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.setAllCaps(false)
        }
    }

}