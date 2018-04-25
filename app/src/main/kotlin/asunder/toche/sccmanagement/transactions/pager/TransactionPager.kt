package asunder.toche.sccmanagement.transactions.pager

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import asunder.toche.sccmanagement.transactions.fragment.TransactionHistoryFragment
import asunder.toche.sccmanagement.transactions.fragment.TransactionListFragment

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class TransactionPager(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    val title = arrayListOf("ซื้อ / ขาย","")
    override fun getItem(position: Int): Fragment = when(position){
        0 -> TransactionListFragment.newInstance()
        1 -> TransactionHistoryFragment.newInstance()
        else -> throw NullPointerException("fragment not attach to view pager")
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return title[position]
    }

}