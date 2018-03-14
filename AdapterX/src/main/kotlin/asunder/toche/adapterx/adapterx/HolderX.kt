package asunder.toche.adapterx.adapterx

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

open class HolderX<out B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root) {
    internal var created = false
}
