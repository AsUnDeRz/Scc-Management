package asunder.toche.sccmanagement.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import asunder.toche.sccmanagement.contact.fragment.ContactFragment
import asunder.toche.sccmanagement.issue.IssueFragment
import asunder.toche.sccmanagement.products.fragment.ProductsFragment
import asunder.toche.sccmanagement.transactions.fragment.TransactionsFragment

/**
 *Created by ToCHe on 25/2/2018 AD.
 */
class MainPager(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    val titles = arrayListOf("ผู้ติดต่อ","ประเด็น","สินค้า","ซื้อ/ขาย")
    override fun getCount(): Int = 4

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> ContactFragment.newInstance()
        1 -> IssueFragment.newInstance()
        2 -> ProductsFragment.newInstance()
        3 -> TransactionsFragment.newInstance()
        else -> throw NullPointerException("fragment not attach to view pager")
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}