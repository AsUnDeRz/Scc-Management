package asunder.toche.sccmanagement.custom

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import asunder.toche.sccmanagement.R

/**
 *Created by ToCHe on 26/5/2018 AD.
 */
class ImageOverlayView : RelativeLayout {

    private var tvDescription: TextView? = null

    private var sharingText: String? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun setDescription(description: String?) {
        tvDescription!!.text = description
    }

    fun setShareText(text: String?) {
        this.sharingText = text
    }

    private fun sendShareIntent() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, sharingText)
        sendIntent.type = "text/plain"
        context.startActivity(sendIntent)
    }

    private fun init() {
        val view = View.inflate(context, R.layout.view_image_overlay, this)
        tvDescription = view.findViewById<View>(R.id.tvDescription) as TextView
        view.findViewById<View>(R.id.btnShare).setOnClickListener { sendShareIntent() }
    }
}