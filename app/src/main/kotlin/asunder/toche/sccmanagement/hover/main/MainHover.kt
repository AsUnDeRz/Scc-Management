package asunder.toche.sccmanagement.hover.main

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import asunder.toche.sccmanagement.BuildConfig
import asunder.toche.sccmanagement.R
import io.mattcarroll.hover.Content
import kotlinx.android.synthetic.main.main_hover.view.*

/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class MainHover : FrameLayout,Content {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    init {
        initUI()
    }

    fun initUI(){
        LayoutInflater.from(context).inflate(R.layout.main_hover, this, true)
        txtVersion.text = "Version ${BuildConfig.VERSION_NAME}"
    }















    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }


    override fun getView(): View {
        return this
    }

    override fun isFullscreen(): Boolean {
        return false
    }

    override fun onShown() {
    }

    override fun onHidden() {
    }

}