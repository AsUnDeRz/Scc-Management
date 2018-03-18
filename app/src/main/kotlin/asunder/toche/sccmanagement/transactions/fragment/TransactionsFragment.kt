package asunder.toche.sccmanagement.transactions.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.transactions.pager.TransactionPager
import kotlinx.android.synthetic.main.fragment_transactions.*

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class TransactionsFragment : Fragment(){

    private val TAG = this::class.java.simpleName
    companion object {
        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_transactions,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPager()
        setUpTablayout()
        setEditAction()
    }



    fun setUpPager(){
        Log.d(TAG,"SetupPager")
       vpTransaction.adapter = TransactionPager(childFragmentManager)
       vpTransaction.setAllowedSwipeDirection(CustomViewPager.SwipeDirection.none)
       vpTransaction.offscreenPageLimit = 2


    }

    fun setUpTablayout(){
        Log.d(TAG,"SetupTablayoutr")
        tabTransaction.setCustomSize(resources.getDimensionPixelSize(R.dimen.txt20).toFloat())
        tabTransaction.setupWithViewPager(vpTransaction)
        tabTransaction.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1){
                    imgNewTransaction.visibility = View.GONE
                }else{
                    imgNewTransaction.visibility = View.VISIBLE
                }
            }
        })
    }


    fun setEditAction(){
        imgNewTransaction.setOnClickListener {
        }
    }
}