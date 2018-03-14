package asunder.toche.adapterx.adapterx

import android.databinding.ViewDataBinding

open class BaseTypeX
@JvmOverloads constructor(open val layout: Int, open val variable: Int? = null)

@Suppress("unused")
abstract class AbsTypeX<B : ViewDataBinding>
@JvmOverloads constructor(layout: Int, variable: Int? = null) : BaseTypeX(layout, variable)

open class ItemTypeX<B : ViewDataBinding>
@JvmOverloads constructor(layout: Int, variable: Int? = null) : AbsTypeX<B>(layout, variable) {
    open fun onCreate(holder: HolderX<B>) { }
    open fun onBind(holder: HolderX<B>) { }
    open fun onRecycle(holder: HolderX<B>) { }
}

open class TypeX<B : ViewDataBinding>
@JvmOverloads constructor(layout: Int, variable: Int? = null) : AbsTypeX<B>(layout, variable) {
    internal var onCreate: ActionX<B>? = null; private set
    internal var onBind: ActionX<B>? = null; private set
    internal var onClick: ActionX<B>? = null; private set
    internal var onLongClick: ActionX<B>? = null; private set
    internal var onRecycle: ActionX<B>? = null; private set
    fun onCreate(action: ActionX<B>?) = apply { onCreate = action }
    fun onBind(action: ActionX<B>?) = apply { onBind = action }
    fun onClick(action: ActionX<B>?) = apply { onClick = action }
    fun onLongClick(action: ActionX<B>?) = apply { onLongClick = action }
    fun onRecycle(action: ActionX<B>?) = apply { onRecycle = action }
}

typealias ActionX<B> = (HolderX<B>) -> Unit
