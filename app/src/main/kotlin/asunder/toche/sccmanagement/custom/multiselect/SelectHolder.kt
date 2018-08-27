package asunder.toche.sccmanagement.custom.multiselect

import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.StateSet
import android.util.TypedValue
import android.view.View
import com.bignerdranch.android.multiselector.MultiSelector
import com.bignerdranch.android.multiselector.MultiSelectorBindingHolder
import com.bignerdranch.android.multiselector.R
import com.bignerdranch.android.multiselector.SelectableHolder

/**
 *Created by ToCHe on 12/5/2018 AD.
 */
open class SelectHolder
@JvmOverloads constructor(itemView: View, mMultiSelector: MultiSelector? = null)
    : MultiSelectorBindingHolder(itemView, mMultiSelector), SelectableHolder {
    private var mIsSelectable: Boolean = false
    var selectionModeBackgroundDrawable: Drawable? = null
        set(selectionModeBackgroundDrawable) {
            field = selectionModeBackgroundDrawable
            if (this.mIsSelectable) {
                this.itemView.background = selectionModeBackgroundDrawable
            }

        }
    var defaultModeBackgroundDrawable: Drawable? = null
        set(defaultModeBackgroundDrawable) {
            field = defaultModeBackgroundDrawable
            if (!this.mIsSelectable) {
                this.itemView.background = this.defaultModeBackgroundDrawable
            }

        }
    var selectionModeStateListAnimator: StateListAnimator? = null
    private var mDefaultModeStateListAnimator: StateListAnimator? = null

    init {
        this.mIsSelectable = false
        if (Build.VERSION.SDK_INT >= 21) {
            this.selectionModeStateListAnimator = getRaiseStateListAnimator(itemView.context)
            this.setDefaultModeStateListAnimator(itemView.stateListAnimator)
        }

        this.selectionModeBackgroundDrawable = getAccentStateDrawable(itemView.context)
        this.defaultModeBackgroundDrawable = itemView.background
    }

    private fun getAccentStateDrawable(context: Context): Drawable {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        val colorDrawable = ColorDrawable(typedValue.data)
        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(16843518), colorDrawable)
        stateListDrawable.addState(StateSet.WILD_CARD, null as Drawable?)
        return stateListDrawable
    }

    private fun getRaiseStateListAnimator(context: Context): StateListAnimator? {
        return if (Build.VERSION.SDK_INT >= 21) AnimatorInflater.loadStateListAnimator(context, R.anim.raise) else null
    }

    fun setSelectionModeStateListAnimator(resId: Int) {
        if (Build.VERSION.SDK_INT >= 21) {
            val animator = AnimatorInflater.loadStateListAnimator(this.itemView.context, resId)
            this.selectionModeStateListAnimator = animator
        }

    }

    fun getDefaultModeStateListAnimator(): StateListAnimator? {
        return this.mDefaultModeStateListAnimator
    }

    fun setDefaultModeStateListAnimator(resId: Int) {
        if (Build.VERSION.SDK_INT >= 21) {
            val animator = AnimatorInflater.loadStateListAnimator(this.itemView.context, resId)
            this.setDefaultModeStateListAnimator(animator)
        }

    }

    fun setDefaultModeStateListAnimator(defaultModeStateListAnimator: StateListAnimator?) {
        this.mDefaultModeStateListAnimator = defaultModeStateListAnimator
    }

    override fun isActivated(): Boolean {
        return this.itemView.isActivated
    }

    override fun setActivated(isActivated: Boolean) {
        this.itemView.isActivated = isActivated
    }

    override fun isSelectable(): Boolean {
        return this.mIsSelectable
    }

    override fun setSelectable(isSelectable: Boolean) {
        val changed = isSelectable != this.mIsSelectable
        this.mIsSelectable = isSelectable
        if (changed) {
            this.refreshChrome()
        }

    }

    private fun refreshChrome() {
        val backgroundDrawable = if (this.mIsSelectable) this.selectionModeBackgroundDrawable else this.defaultModeBackgroundDrawable
        this.itemView.background = backgroundDrawable
        backgroundDrawable?.jumpToCurrentState()

        if (Build.VERSION.SDK_INT >= 21) {
            val animator = if (this.mIsSelectable) this.selectionModeStateListAnimator else this.mDefaultModeStateListAnimator
            this.itemView.stateListAnimator = animator
            animator?.jumpToCurrentState()
        }

    }
}