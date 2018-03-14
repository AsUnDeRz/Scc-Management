package asunder.toche.adapterx.pageradapterx

import android.support.v4.view.ViewPager

inline fun ViewPager.lastPagerAdapterX(modelId: Int, crossinline f: PagerAdapterX.() -> Unit) =
		PagerAdapterX(modelId).apply {
			f()
			into(this@lastPagerAdapterX)
		}