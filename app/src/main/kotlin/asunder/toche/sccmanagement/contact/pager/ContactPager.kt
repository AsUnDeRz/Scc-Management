package asunder.toche.sccmanagement.contact.pager

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import asunder.toche.sccmanagement.contact.fragment.CompanyFragment
import asunder.toche.sccmanagement.contact.fragment.HistoryCompanyFragment

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class ContactPager(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    val titles = arrayListOf("บริษัท","ประวัติ")
    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> CompanyFragment.newInstance()
        1 -> HistoryCompanyFragment.newInstance()
        else -> throw NullPointerException("fragment not attach to view pager")
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

}