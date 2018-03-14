package asunder.toche.sccmanagement.custom.extension

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar

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

fun EditText.DisableClick(){
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
    this.isFocusable = false
    this.isFocusableInTouchMode = false
}
