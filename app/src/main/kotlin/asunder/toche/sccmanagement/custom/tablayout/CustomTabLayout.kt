package asunder.toche.sccmanagement.custom.tablayout

import android.content.Context
import android.graphics.Typeface
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.text.AllCapsTransformationMethod
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.ViewGroup
import asunder.toche.sccmanagement.R

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class CustomTabLayout(context: Context?, attrs: AttributeSet?) : TabLayout(context, attrs) {

    private var customSize :Float? = null



    override fun setupWithViewPager(viewPager: ViewPager?) {
        super.setupWithViewPager(viewPager)

        val mTypeface = Typeface.createFromAsset(context.assets, "fonts/set_bold.ttf")
        if (mTypeface != null) {
            this.removeAllTabs()
            val slidingTabStrip = getChildAt(0) as ViewGroup
            val adapter = viewPager!!.adapter

            var i = 0
            val count = adapter!!.count
            while (i < count) {
                val tab = this.newTab()
                this.addTab(tab.setText(adapter.getPageTitle(i)))
                val view = (slidingTabStrip.getChildAt(i) as ViewGroup).getChildAt(1) as AppCompatTextView
                view.setTypeface(mTypeface, Typeface.NORMAL)
                view.setAllCaps(false)
                view.textSize = if (customSize == null)
                    resources.getDimensionPixelSize(R.dimen.txt18).toFloat()
                else customSize!!
                i++
                //resources.getDimensionPixelSize(R.dimen.txt18).toFloat()
            }
        }
    }

    fun setCustomSize(size:Float){
        customSize = size
    }

}