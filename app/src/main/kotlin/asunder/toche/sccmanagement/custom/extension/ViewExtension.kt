package asunder.toche.sccmanagement.custom.extension

import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.text.InputFilter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_edit_contact.*

/**
 *Created by ToCHe on 10/3/2018 AD.
 */

fun ViewGroup.createView(layout: Int): View = LayoutInflater.from(
        this.context!!).inflate(layout, this, false)

fun View.getColor(color: Int): Int = ContextCompat.getColor(this.context, color)

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun ProgressBar.showLoading() {
    if (!this.isShown) this.visible()
}

fun ProgressBar.hideLoading() {
    if (this.isShown) this.gone()
}

fun SwipeRefreshLayout.showRefresh() {
    if (!this.isRefreshing) {
        this.isRefreshing = true
    }
}

fun SwipeRefreshLayout.hideRefresh() {
    if (this.isRefreshing) {
        this.isRefreshing = false
    }
}
fun EditText.EnableClick(){
    this.isFocusable = true
    this.isFocusableInTouchMode = true
}

fun EditText.DisableClick(){
    this.isFocusable = false
    this.isFocusableInTouchMode = false
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun EditText.ShowKeyboard(){
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.limitLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

fun EditText.ShowScrollBar(){
    this.setOnTouchListener { view, event ->
        if (view?.id == this.id) {
            view.parent.requestDisallowInterceptTouchEvent(true)
            when(event?.action){
                MotionEvent.ACTION_UP ->{
                    view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        false
    }
}

fun RecyclerView.ShowScrollBar(){
    this.setOnTouchListener { view, event ->
        if (view?.id == this.id) {
            view.parent.requestDisallowInterceptTouchEvent(true)
            when(event?.action){
                MotionEvent.ACTION_UP ->{
                    view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        false
    }
}

fun RecyclerView.SetHeight(size:Int){
    val newHeight:Float = if (size > 5){
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics)
    }else{
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ((40*size).toFloat()), resources.displayMetrics)
    }
    val params = this.layoutParams
    params.height = newHeight.toInt()
    this.layoutParams = params
}