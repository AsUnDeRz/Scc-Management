package asunder.toche.sccmanagement.products.pager

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import asunder.toche.sccmanagement.products.fragment.ProductHistoryFragment
import asunder.toche.sccmanagement.products.fragment.ProductListFragment

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class ProductsPager(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {


    val title = arrayListOf("สินค้า","ขายให้ใครบ้าง")
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> ProductListFragment.newInstance()
        1 -> ProductHistoryFragment.newInstance()
        else -> throw NullPointerException("fragment not attach to view pager")
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return title[position]
    }

}