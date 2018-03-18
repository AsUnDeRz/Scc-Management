package asunder.toche.sccmanagement.main

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager

/**
 *Created by ToCHe on 17/3/2018 AD.
 */
class FilterViewPager(fm: FragmentManager?,var isIssue:Boolean) : FragmentStatePagerAdapter(fm) {

    private val titleIssue = arrayListOf("ทั้งหมด","วันนี้","พรุ่งนี้","เมื่อวาน")
    private val titleTransaction = arrayListOf("วันนี้","พรุ่งนี้","เมื่อวาน")
    override fun getItem(position: Int): Fragment? {
        return null
    }

    override fun getCount(): Int {
        return if (isIssue){
            titleIssue.size
        }else{
            titleTransaction.size
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if(isIssue){
            titleIssue[position]
        }else{
            titleTransaction[position]
        }
    }

}