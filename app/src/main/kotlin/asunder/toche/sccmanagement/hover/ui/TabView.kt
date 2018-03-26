package asunder.toche.sccmanagement.hover.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.util.TypedValue
import android.view.View
import com.onegravity.contactpicker.Helper
import com.onegravity.contactpicker.picture.ContactBadge.STANDARD_PICTURE_SIZE
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.Paint.Align
import android.support.v4.view.ViewCompat.getClipBounds





/**
 *Created by ToCHe on 26/3/2018 AD.
 */
//context: Context, private val mCircleDrawable: Drawable, private var mIconDrawable: Drawable?
class TabView : View {

    constructor(context: Context,circleDrawable: Drawable,icon: Drawable?) : super(context){
        mContext = context
        mCircleDrawable = circleDrawable
        mIconDrawable = icon
    }

    constructor(context: Context,circleDrawable: Drawable,title:String) : super(context){
        mContext = context
        mCircleDrawable = circleDrawable
        mChar = title
    }

    private var mContext:Context
    private var mCircleDrawable: Drawable
    private var mIconDrawable: Drawable? = null
    private var mBackgroundColor: Int = 0
    private var mForegroundColor: Int? = null
    private var mIconInsetLeft: Int = 0
    private var mIconInsetTop: Int = 0
    private var mIconInsetRight: Int = 0
    private var mIconInsetBottom: Int = 0
    private var mChar: String? = null
    private lateinit var mTextPaint: Paint
    private lateinit var mRect: Rect
    private val mSizeInPx: Int
    private val mDensity: Float


    init {
        mDensity = Helper.getDisplayMetrics(context).density
        mSizeInPx = Math.round(STANDARD_PICTURE_SIZE * mDensity)
        init()
    }

    private fun init() {
        val insetsDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics).toInt()
        mIconInsetBottom = insetsDp
        mIconInsetRight = mIconInsetBottom
        mIconInsetTop = mIconInsetRight
        mIconInsetLeft = mIconInsetTop
        val customFont = Typeface.createFromAsset(context.assets,
                "fonts/set_bold.ttf")
        mTextPaint = Paint()
        mTextPaint.isAntiAlias = true
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.setARGB(255, 255, 255, 255)
        mTextPaint.textSize = mSizeInPx * 0.7f // just scale this down a bit
        mTextPaint.typeface = customFont
        mRect = Rect()
    }

    fun setTabBackgroundColor(@ColorInt backgroundColor: Int) {
        mBackgroundColor = backgroundColor
        mCircleDrawable.setColorFilter(mBackgroundColor, PorterDuff.Mode.SRC_ATOP)
    }

    fun setTabForegroundColor(@ColorInt foregroundColor: Int?) {
        mForegroundColor = foregroundColor
        if (null != mForegroundColor) {
            mIconDrawable?.setColorFilter(mForegroundColor!!, PorterDuff.Mode.SRC_ATOP)
        } else {
            mIconDrawable?.colorFilter = null
        }
    }

    fun setIcon(icon: Drawable?) {
        mIconDrawable = icon
        if (null != mForegroundColor && null != mIconDrawable) {
            mIconDrawable?.setColorFilter(mForegroundColor!!, PorterDuff.Mode.SRC_ATOP)
        }
        updateIconBounds()

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Make circle as large as View minus padding.
        mCircleDrawable.setBounds(paddingLeft, paddingTop, w - paddingRight, h - paddingBottom)

        // Re-size the icon as necessary.
        updateIconBounds()

        invalidate()
    }

    private fun updateIconBounds() {
        if (null != mIconDrawable) {
            val bounds = Rect(mCircleDrawable.bounds)
            bounds.set(bounds.left + mIconInsetLeft, bounds.top + mIconInsetTop, bounds.right - mIconInsetRight, bounds.bottom - mIconInsetBottom)
            mIconDrawable?.bounds = bounds
        }
    }


    override fun onDraw(canvas: Canvas) {
        mCircleDrawable.draw(canvas)
        if (null != mIconDrawable) {
            mIconDrawable?.draw(canvas)
        }else{
            // letter
            canvas.getClipBounds(mRect)
            val cHeight = mRect.height()
            val cWidth = mRect.width()
            mTextPaint.textAlign = Paint.Align.LEFT
            mTextPaint.getTextBounds(mChar, 0, 1, mRect)
            val x = cWidth / 2f - mRect.width() / 2f - mRect.left
            val y = cHeight / 2f + mRect.height() / 2f - mRect.bottom
            val width = mTextPaint.measureText(mChar)
            //canvas.drawText(mChar, (mSizeInPx - width) / 2f, (mSizeInPx + mRect.height()) / 2f, mTextPaint)
            canvas.drawText(mChar, x, y,mTextPaint)
        }
    }
}
