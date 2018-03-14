package asunder.toche.sccmanagement.custom.button

import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import asunder.toche.sccmanagement.R
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.internal.CommonUtils
import java.lang.ref.WeakReference

/**
 *Created by ToCHe on 25/2/2018 AD.
 */
open class LoginTwitter : Button {

    internal val TAG = TwitterCore.TAG
    internal val ERROR_MSG_NO_ACTIVITY = "TwitterLoginButton requires an activity." + " Override getActivity to provide the activity for this button."
    internal var activityRef: WeakReference<Activity>? = null
    @Volatile
    internal var authClient: TwitterAuthClient? = null
    internal var onClickListener: View.OnClickListener? = null
    internal var callback: Callback<TwitterSession>? = null

    constructor(context: Context) : this(context,null)

    constructor(context: Context, attributeSet: AttributeSet?) :this(context,attributeSet, R.attr.buttonStyle)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
            super(context, attributeSet, defStyleAttr){
        this.activityRef = WeakReference(context as Activity)
        this.authClient = null
        setupButton()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupButton() {
        val res = resources
        super.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context,com.twitter.sdk.android.core.R.drawable.tw__ic_logo_default), null, null, null)
        super.setCompoundDrawablePadding(
                res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_drawable_padding))
        super.setTextColor(ContextCompat.getColor(context,com.twitter.sdk.android.core.R.color.tw__solid_white))
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimensionPixelSize(R.dimen.login_btn_text_size).toFloat())
        super.setTypeface(Typeface.createFromAsset(context.assets, "fonts/set_bold.ttf"))
        super.setPadding(res.getDimensionPixelSize(R.dimen.login_btn_left_padding), 0,
                res.getDimensionPixelSize(R.dimen.login_btn_right_padding), 0)
        super.setBackgroundResource(com.twitter.sdk.android.core.R.drawable.tw__login_btn)
        super.setOnClickListener(LoginClickListener())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.setAllCaps(false)
        }
    }

    fun setCallback(callback: Callback<TwitterSession>?) {
        if (callback == null) {
            throw IllegalArgumentException("Callback cannot be null")
        }
        this.callback = callback
    }

    fun getCallback(): Callback<TwitterSession>? {
        return callback
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == getTwitterAuthClient()?.requestCode) {
            getTwitterAuthClient()?.onActivityResult(requestCode, resultCode, data)
        }
    }


    protected fun getActivity(): Activity? {
        return if (context is ContextThemeWrapper && (context as ContextThemeWrapper).baseContext is Activity) {
            (context as ContextThemeWrapper).baseContext as Activity
        } else if (context is Activity) {
            context as Activity
        } else if (isInEditMode) {
            null
        } else {
            throw IllegalStateException(ERROR_MSG_NO_ACTIVITY)
        }
    }

    override fun setOnClickListener(onClickListener: View.OnClickListener?) {
        this.onClickListener = onClickListener
    }

    private inner class LoginClickListener : View.OnClickListener {

        override fun onClick(view: View) {
            checkCallback(callback)
            checkActivity(activityRef?.get())

            getTwitterAuthClient()?.authorize(activityRef?.get(), callback)

            if (onClickListener != null) {
                onClickListener?.onClick(view)
            }
        }

        private fun checkCallback(callback: Callback<*>?) {
            if (callback == null) {
                CommonUtils.logOrThrowIllegalStateException(TwitterCore.TAG,
                        "Callback must not be null, did you call setCallback?")
            }
        }

        private fun checkActivity(activity: Activity?) {
            if (activity == null || activity.isFinishing) {
                CommonUtils.logOrThrowIllegalStateException(TwitterCore.TAG,
                        ERROR_MSG_NO_ACTIVITY)
            }
        }
    }

    internal fun getTwitterAuthClient(): TwitterAuthClient? {
        if (authClient == null) {
            synchronized(TwitterLoginButton::class.java) {
                if (authClient == null) {
                    authClient = TwitterAuthClient()
                }
            }
        }
        return authClient
    }

    private fun checkTwitterCoreAndEnable() {
        //Default (Enabled) in edit mode
        if (isInEditMode) return

        try {
            TwitterCore.getInstance()
        } catch (ex: IllegalStateException) {
            //Disable if TwitterCore hasn't started
            Twitter.getLogger().e(TAG, ex.message)
            isEnabled = false
        }

    }
}