package asunder.toche.sccmanagement.hover

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import asunder.toche.sccmanagement.issue.IssueFragment
import asunder.toche.sccmanagement.products.fragment.ProductsFragment
import asunder.toche.sccmanagement.transactions.fragment.TransactionsFragment

/**
 *Created by ToCHe on 27/3/2018 AD.
 */
class HoverPager(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    val titles = arrayListOf("ประเด็น","สินค้า","ซื้อ/ขาย")
    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> IssueFragment.newInstance()
        1 -> ProductsFragment.newInstance()
        2 -> TransactionsFragment.newInstance()
        else -> throw NullPointerException("fragment not attach to view pager")
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}