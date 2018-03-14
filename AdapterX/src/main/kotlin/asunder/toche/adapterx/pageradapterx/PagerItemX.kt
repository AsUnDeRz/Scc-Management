package asunder.toche.adapterx.pageradapterx

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import java.lang.ref.SoftReference

data class PagerItemX(
		val layoutId: Int,
		val model: Any?,
		val title: CharSequence?,
		val width: Float,
		val adapter : RecyclerView.Adapter<*>?,
		val layoutManager: RecyclerView.LayoutManager
) {

    var binding: ViewDataBinding?
		get() = bindingRef?.get()
		internal set(value) {
			bindingRef = SoftReference(value)
		}

	private var bindingRef: SoftReference<ViewDataBinding?>? = null
}
