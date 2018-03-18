package asunder.toche.sccmanagement.transactions.fragment

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.main.FilterViewPager
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.transactions.adapter.SaleRateAdapter
import asunder.toche.sccmanagement.transactions.pager.TransactionPager
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.android.synthetic.main.fragment_transactions_add.*
import kotlinx.android.synthetic.main.layout_price_rate.*
import kotlinx.android.synthetic.main.section_transaction_confirm.*
import kotlinx.android.synthetic.main.section_transaction_info.*

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class TransactionsFragment : Fragment(){

    private val TAG = this::class.java.simpleName
    companion object {
        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }
    private lateinit var rootLayoutPriceRate : ScrollView
    private lateinit var rootTransactionForm : ConstraintLayout


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
        observerTabFilterTransactions()
        inflateStubTransactionAdd()
        inflateStubLayoutPriceRate()
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
            showTransactionForm()
        }
    }

    fun observerTabFilterTransactions(){
        val viewPager = ViewPager(context!!)
        viewPager.adapter = FilterViewPager(fragmentManager,false)
        tabLayoutFilterTransaction.setupWithViewPager(viewPager)
        tabLayoutFilterTransaction.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 ->{
                        //today
                        //separateSection(issueVM.sortToday())
                    }
                    1 ->{
                        //tomorrow
                        //separateSection(issueVM.sortTomorrow())
                    }
                    2 ->{
                        //yesterday
                        // separateSection(issueVM.sortYesterday())
                    }
                }
            }
        })
    }

    fun inflateStubLayoutPriceRate(){
        stubPriceRate.setOnInflateListener { _, v ->
            rootLayoutPriceRate = v as ScrollView
            rootLayoutPriceRate.visibility = View.GONE
        }
        stubPriceRate.inflate()
        initViewInStubPriceRate()

    }

    fun inflateStubTransactionAdd(){
        stubTransactionAdd.setOnInflateListener { _, v ->
            rootTransactionForm = v as ConstraintLayout
            rootTransactionForm.visibility = View.GONE

        }
        stubTransactionAdd.inflate()
        initViewInStubTransactionForm()

    }

    fun initViewInStubTransactionForm(){
        btnCancelTransaction.setOnClickListener {
            showTransactionList()

        }
        btnSaveTransactionOnBottom.setOnClickListener {
            showTransactionList()
        }
        btnSaveTransactionOnTop.setOnClickListener {
            showTransactionList()
        }

        imgNewSaleRate.setOnClickListener {
            showPricePriceForm()
            txtTitlePrice.text = "ราคาลูกค้า"
        }

        rvSalePrice.apply {
            val data : MutableList<Model.SalePrice> = mutableListOf()
            for(i in 0 until 10){
                data.add(Model.SalePrice("1000$i",true,"10$i",
                                Utils.getCurrentDateShort(),""))
            }
            data.sortByDescending { it.date }
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = SaleRateAdapter(data)
        }

    }

    fun initViewInStubPriceRate(){

        btnSaveRate.setOnClickListener {
            showTransactionForm()
        }

        btnCancelRate.setOnClickListener {
            showTransactionForm()
        }

    }

    fun showTransactionList(){
        rootTransactionForm.visibility = View.GONE
        rootLayoutPriceRate.visibility = View.GONE
        imgNewTransaction.visibility = View.VISIBLE

    }
    fun showTransactionForm(){
        transactionScrollView.fullScroll(ScrollView.FOCUS_UP)
        rootTransactionForm.visibility = View.VISIBLE
        rootLayoutPriceRate.visibility = View.GONE
        imgNewTransaction.visibility = View.GONE

    }

    fun showPricePriceForm(){
        rootTransactionForm.visibility = View.GONE
        rootLayoutPriceRate.visibility = View.VISIBLE

    }
}