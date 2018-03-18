package asunder.toche.sccmanagement.issue.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager

/**
 *Created by ToCHe on 17/3/2018 AD.
 */
class IssueFilterViewPager(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    val title = arrayListOf("ทั้งหมด","วันนี้","พรุ่งนี้","เมื่อวาน")
    override fun getItem(position: Int): Fragment? {
        return null
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence {
        return title[position]
    }

}