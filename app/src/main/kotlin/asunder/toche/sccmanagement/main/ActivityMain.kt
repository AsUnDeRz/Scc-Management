package asunder.toche.sccmanagement.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.auth.ActivityLogin
import asunder.toche.sccmanagement.auth.ActivityManagement
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.TriggerUpdate
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.hover.HoverService
import asunder.toche.sccmanagement.issue.IssueState
import asunder.toche.sccmanagement.issue.IssueViewModel
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.viewmodel.ProductViewModel
import asunder.toche.sccmanagement.service.ManageUserService
import asunder.toche.sccmanagement.settings.ActivitySetting
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_drawer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ActivityMain : AppCompatActivity(), LifecycleOwner{


    companion object {
        var isActivityResume : Boolean? = null
    }


    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var actionBar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var controllViewModel: ControllViewModel
    lateinit var contactVM : ContactViewModel
    lateinit var productVM : ProductViewModel
    lateinit var issueVM : IssueViewModel
    lateinit var transactionVM : TransactionViewModel
    private val lifecycleRegistry by lazy {
        android.arch.lifecycle.LifecycleRegistry(this)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controllViewModel = ViewModelProviders.of(this).get(ControllViewModel::class.java)
        contactVM = ViewModelProviders.of(this).get(ContactViewModel::class.java)
        productVM = ViewModelProviders.of(this).get(ProductViewModel::class.java)
        issueVM = ViewModelProviders.of(this).get(IssueViewModel::class.java)
        transactionVM = ViewModelProviders.of(this).get(TransactionViewModel::class.java)
        setContentView(R.layout.activity_main)
        setUpPager()
        setUpTablayout()
        setHumburgerButton()
        observerContacts()
        searchTextChanged()
        controllViewModel.updateCurrentUI(ROOT.CONTACTS)
        HoverService.showFloatingMenu(this)
        checkDataFromService(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            if (intent.hasExtra(ROOT.MOBILE)){
                Utils.findCompany(intent.getStringExtra(ROOT.MOBILE),
                        object : Utils.OnFindCompanyListener{
                            override fun onResults(results: MutableList<Model.Contact>) {
                                contactVM.updateContact(results.first())
                                contactVM.updateViewState(ContactState.SELECTCONTACT)
                            }
                        },contactVM.service.getContactInDb())
            }else {
                checkDataFromService(it)
            }
        }
    }

    private fun checkDataFromService(intent: Intent){
        if (intent.hasExtra(ROOT.ISSUE)){
            pager.currentItem = 1
            issueVM.updateViewState(IssueState.TRIGGERFROMSERVICE)
            issueVM.updateCurrentIssue(intent.getParcelableExtra(ROOT.ISSUE))
        }
        if (intent.hasExtra(ROOT.TRANSACTIONS)){
            pager.currentItem = 3
            transactionVM.updateStateView(TransactionState.SHOWTRANSACTION)
            transactionVM.updateTransaction(intent.getParcelableExtra(ROOT.TRANSACTIONS))
        }
    }


    fun setUpPager(){
        pager.adapter = MainPager(supportFragmentManager)
        pager.setAllowedSwipeDirection(CustomViewPager.SwipeDirection.none)
        pager.offscreenPageLimit = 3

    }

    fun setUpTablayout(){
        tabLayout.setupWithViewPager(pager)
        tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 ->{
                        controllViewModel.updateCurrentUI(ROOT.CONTACTS)
                        System.out.println("Tab Select ${tab.position}")
                    }
                    1 ->{
                        controllViewModel.updateCurrentUI(ROOT.ISSUE)
                        System.out.println("Tab Select ${tab.position}")
                    }
                    2 ->{
                        controllViewModel.updateCurrentUI(ROOT.PRODUCTS)
                        System.out.println("Tab Select ${tab.position}")
                    }
                    3 ->{
                        controllViewModel.updateCurrentUI(ROOT.TRANSACTIONS)
                        System.out.println("Tab Select ${tab.position}")
                    }
                }
            }

        })
    }

    fun setHumburgerButton() {
        actionBar = findViewById(R.id.toolbar)
        setSupportActionBar(actionBar)

        drawerLayout = findViewById(R.id.drawerLayout)


        actionBarDrawerToggle = ActionBarDrawerToggle(this
                ,drawerLayout
                ,R.string.open_drawer
                ,R.string.close_drawer)
        actionBarDrawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this,android.R.color.black)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setOnClickMenu()

    }

    fun setOnClickMenu(){
        val btns = arrayOf(btnContact,btnIssue,btnProduct,btnTransaction,btnAdmin,btnLogout,btnSetting)
        val isAdmin = intent.extras.getBoolean(ROOT.ADMIN)
        if (isAdmin){
            btnAdmin.visibility = View.VISIBLE
        }else{
            btnAdmin.visibility = View.GONE
        }
        for (button in btns){
            button.setOnClickListener {
                when (button) {
                    btnContact -> {
                        drawerLayout.closeDrawers()
                        btnAdmin.visibility = View.GONE
                        pager.currentItem = 0
                    }
                    btnIssue -> {
                        drawerLayout.closeDrawers()
                        pager.currentItem = 1
                    }
                    btnProduct -> {
                        drawerLayout.closeDrawers()
                        pager.currentItem = 2
                    }
                    btnTransaction -> {
                        drawerLayout.closeDrawers()
                        pager.currentItem = 3
                    }
                    btnAdmin -> {
                        startActivity(Intent().setClass(this@ActivityMain,
                                ActivityManagement::class.java))
                    }
                    btnLogout -> {
                        ManageUserService().signOut()
                        startActivity(Intent().setClass(this@ActivityMain,
                                ActivityLogin::class.java))
                        finish()
                    }
                    btnSetting -> {
                        startActivityForResult(Intent().setClass(this@ActivityMain,
                                ActivitySetting::class.java),KEY.OPEN_SETTING)
                    }
                }

            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY.OPEN_SETTING) {
            triggerUpdate()
        }

    }

    @Subscribe
    fun triggerUpdate(){
        EventBus.getDefault().postSticky(TriggerUpdate(true))
    }

    fun observerContacts(){
        contactVM.isSaveContactComplete.observe(this, Observer {
            when(it){
                ContactState.ALLCONTACT ->{
                }
                ContactState.NEWCONTACT ->{
                }
                ContactState.EDITCONTACT ->{
                }
                ContactState.SELECTCONTACT ->{
                }
                ContactState.NEWISSUE ->{
                    pager.currentItem = 1
                    issueVM.updateViewState(IssueState.NEWFROMCONTACT)
                }
                ContactState.NEWTRANSACTION ->{
                    pager.currentItem = 3
                    transactionVM.updateStateView(TransactionState.NEWFROMCONTACT)
                }
            }
        })
        contactVM.contact.observe(this, Observer {
            txtSearch.setText(it?.company?.trim())
        })
        productVM.product.observe(this, Observer {
            txtSearch.setText(it?.product_name?.trim())
        })

        transactionVM.stateView.observe(this, Observer {
            if (it == TransactionState.SHOWTRANSACTION && pager.currentItem == 2){
                //tabLayout.setScrollPosition(3,0f,true)
                pager.currentItem = 3
                //transactionVM.updateStateView(TransactionState.SHOWTRANSACTION)
            }
        })


    }

    fun searchTextChanged(){
        txtSearch.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when(pager.currentItem){
                    0 ->{
                        Utils.findCompany(s.toString(),object :Utils.OnFindCompanyListener{
                            override fun onResults(results: MutableList<Model.Contact>) {
                                contactVM.updateContacts(results)
                            }
                        },contactVM.service.getContactInDb())
                    }
                    1 ->{

                    }
                    2 ->{
                        Utils.findProduct(s.toString(),object : Utils.OnFindProductListener{
                            override fun onResults(results: MutableList<Model.Product>) {
                                productVM.updateProducts(results)
                            }
                        },productVM.service.getProductsInDb())
                    }
                    3 ->{

                    }
                }

                //change icon search
                if (s.isNullOrEmpty()){
                    Glide.with(this@ActivityMain)
                            .load(R.drawable.ic_search_black_36dp)
                            .into(imgSearch)
                }else{
                    Glide.with(this@ActivityMain)
                            .load(R.drawable.ic_close_black_24dp)
                            .into(imgSearch)
                    imgSearch.setOnClickListener {
                        txtSearch.text.clear()
                    }
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        isActivityResume = true
    }

    override fun onPause() {
        super.onPause()
        isActivityResume = false
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

}
