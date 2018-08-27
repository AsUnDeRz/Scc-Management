package asunder.toche.sccmanagement.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import asunder.toche.sccmanagement.BuildConfig
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.FileEnDecryptManager
import asunder.toche.sccmanagement.custom.TriggerImage
import asunder.toche.sccmanagement.custom.TriggerProduct
import asunder.toche.sccmanagement.custom.TriggerUpdate
import asunder.toche.sccmanagement.custom.dialog.ConfirmDialog
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.hover.FloatingViewService
import asunder.toche.sccmanagement.issue.IssueState
import asunder.toche.sccmanagement.issue.IssueViewModel
import asunder.toche.sccmanagement.preference.Contextor
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.ProductState
import asunder.toche.sccmanagement.products.viewmodel.ProductViewModel
import asunder.toche.sccmanagement.settings.ActivitySetting
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import com.bumptech.glide.Glide
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_drawer.*
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.crypto.Cipher

class ActivityMain : AppCompatActivity(), LifecycleOwner,ConfirmDialog.ConfirmDialogListener{

    companion object {
        var isActivityResume : Boolean? = null
    }


    val TAG = this::class.java.simpleName
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var actionBar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var controlViewModel: ControlViewModel
    lateinit var contactVM : ContactViewModel
    lateinit var productVM : ProductViewModel
    lateinit var issueVM : IssueViewModel
    lateinit var transactionVM : TransactionViewModel

    private val lifecycleRegistry by lazy {
        android.arch.lifecycle.LifecycleRegistry(this)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Contextor.getInstance().init(this)
        controlViewModel = ViewModelProviders.of(this).get(ControlViewModel::class.java)
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
        controlViewModel.updateCurrentUI(ROOT.CONTACTS)
        checkDataFromService(intent)
        setupCurrentUser()
        if (intent.hasExtra(ROOT.CONTACTS)){
            val contact = intent.getParcelableExtra<Model.Contact>(ROOT.CONTACTS)
            selectContact(contact)
        }
        addContentListener()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            when {
                intent.hasExtra(ROOT.CONTACTS) -> {
                    val contact = intent.getParcelableExtra<Model.Contact>(ROOT.CONTACTS)
                    selectContact(contact)
                }
                intent.hasExtra(ROOT.MOBILE) -> Utils.findCompany(intent.getStringExtra(ROOT.MOBILE),
                        object : Utils.OnFindCompanyListener{
                            override fun onResults(results: MutableList<Model.Contact>) {
                                selectContact(results.first())
                            }
                        },contactVM.service.getContactInDb())
                else -> checkDataFromService(it)
            }
        }
    }

    fun selectContact(contact: Model.Contact){
        contactVM.updateContact(contact)
        contactVM.updateViewState(ContactState.SELECTCONTACT)
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
                updateCurrentUI(tab)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateCurrentUI(tab)
            }
        })
    }

    fun updateCurrentUI(tab:TabLayout.Tab?){
        txtSearch.text.clear()
        when(tab?.position){
            0 ->{
                filterContact(txtSearch.text.toString())
                controlViewModel.updateCurrentUI(ROOT.CONTACTS)
                System.out.println("Tab Select ${tab.position}")
            }
            1 ->{
                controlViewModel.updateCurrentUI(ROOT.ISSUE)
                System.out.println("Tab Select ${tab.position}")
            }
            2 ->{
                controlViewModel.updateCurrentUI(ROOT.PRODUCTS)
                System.out.println("Tab Select ${tab.position}")
            }
            3 ->{
                controlViewModel.updateCurrentUI(ROOT.TRANSACTIONS)
                System.out.println("Tab Select ${tab.position}")
            }
        }
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
        val btns = arrayOf(btnContact,btnIssue,btnProduct,btnTransaction,btnSetting)
        for (button in btns){
            button.setOnClickListener {
                when (button) {
                    btnContact -> {
                        drawerLayout.closeDrawers()
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
                ContactState.SAVED ->{
                    txtSearch.text.clear()
                }
                ContactState.ALLCONTACT ->{
                    txtSearch.text.clear()
                }
                ContactState.NEWCONTACT ->{
                }
                ContactState.EDITCONTACT ->{
                }
                ContactState.SELECTCONTACT ->{
                    pager.currentItem = 0
                    val lines = contactVM.contact.value?.company?.lines()
                    lines?.let {
                        txtSearch.setText(it[0])
                    }
                }
                ContactState.NEWISSUE ->{
                    pager.currentItem = 1
                    issueVM.updateViewState(IssueState.NEWFROMCONTACT)
                }
                ContactState.NEWTRANSACTION ->{
                    pager.currentItem = 3
                    transactionVM.updateStateView(TransactionState.NEWFROMCONTACT)
                }
                ContactState.TRIGGERFROMTRANSACTION ->{
                    pager.currentItem = 0
                }
                ContactState.TRIGGERFROMISSUE ->{
                    pager.currentItem = 0
                }
                ContactState.SHOWFORM ->{
                    //pager.currentItem = 0
                }
            }
        })
        contactVM.contact.observe(this, Observer {
            val lines = it?.company?.trim()?.lines()
            lines?.let {
                txtSearch.setText(it[0])
            }
        })
        productVM.product.observe(this, Observer {
            val lines = it?.product_name?.trim()?.lines()
            lines?.let {
                txtSearch.setText(it[0])
            }
        })

        productVM.stateView.observe(this, Observer {
            when(it){
                ProductState.TRIGGERFROMTRANSACTION ->{
                    pager.currentItem = 2
                }
                ProductState.SHOWLIST ->{
                    pager.currentItem = 2
                    txtSearch.text.clear()
                }
            }
        })

        transactionVM.stateView.observe(this, Observer {
            if (it == TransactionState.SHOWTRANSACTION || it == TransactionState.SHOWFORM ||
                    it == TransactionState.TRIGGERFROMSERVICE){
                //tabLayout.setScrollPosition(3,0f,true)
                pager.currentItem = 3
                //transactionVM.updateStateView(TransactionState.SHOWTRANSACTION)
            }
        })

        issueVM.isSaveIssueComplete.observe(this,Observer{
            when(it){
                IssueState.SHOWFROM ->{
                    pager.currentItem = 1
                }
                IssueState.TRIGGERFROMSERVICE ->{
                    pager.currentItem = 1
                }
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
                        filterContact(s.toString())
                    }
                    1 ->{
                        filterIssue(s.toString())
                    }
                    2 ->{
                        filterProduct(s.toString())
                    }
                    3 ->{
                        filterTransactions(s.toString())
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
        async(UI) {
            withContext(DefaultDispatcher) {
            openFloatingView()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityResume = false
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onBackPressed() {
        val confirmDialog = ConfirmDialog.newInstance("คุณต้องการออกจากแอฟพลิเคชั่นใช่หรือไม่","แจ้งเตือน",true)
        confirmDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        confirmDialog.customListener(this)
        confirmDialog.customTextButton("ออกจากโปรแกรม","ใช้ต่อ")
        confirmDialog.show(supportFragmentManager, ConfirmDialog::class.java.simpleName)
    }

    override fun onClickConfirm() {
        /*
        val key = "This is a secret"
        val storage = Storage(applicationContext)
        val files = storage.getNestedFiles(Utils.getPath(this))
        async{
            files.forEach {
                FileEnDecryptManager.getInstance().fileProcessor(Cipher.ENCRYPT_MODE, key,it,it)
            }
        }
        */


        openFloatingView()
        val setIntent = Intent(Intent.ACTION_MAIN)
        setIntent.addCategory(Intent.CATEGORY_HOME)
        setIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(setIntent)


    }

    override fun onClickCancel() {
        /*
        val key = "This is a secret"
        val storage = Storage(applicationContext)
        val files = storage.getNestedFiles(Utils.getPath(this))
        files.forEach {
                FileEnDecryptManager.getInstance().fileProcessor(Cipher.DECRYPT_MODE, key,it,it)
        }
        */

    }

    fun setupCurrentUser(){
        /*
        val user = FirebaseAuth.getInstance().currentUser
        if (user!!.uid == Prefer.getUUID(this)){
            txtAccount.text = "Email : ${user?.email} \n Uid : ${user?.uid}"
        }else{
            txtAccount.text = "Email : ${user?.email} \n Uid : current uid not match in Preference "
        }
        */
        txtAccount.text = "Version "+BuildConfig.VERSION_NAME
    }


    fun filterContact(query:String){
        Utils.findCompany(query,object :Utils.OnFindCompanyListener{
            override fun onResults(results: MutableList<Model.Contact>) {
                contactVM.updateContacts(results)
            }
        },contactVM.service.getContactInDb())
    }

    fun filterIssue(query:String){
        Utils.findIssue(query,object : Utils.OnFindIssueListener{
            override fun onResults(results: MutableList<Model.Issue>) {
                issueVM.updateIssues(results)
            }
        },issueVM.service.getIssueInDb())
    }

    fun filterProduct(query:String){
        Utils.findProduct(query,object : Utils.OnFindProductListener{
            override fun onResults(results: MutableList<Model.Product>) {
                productVM.updateProducts(results)
            }
        },productVM.service.getProductsInDb())
    }

    fun filterTransactions(query:String){
        Utils.findTransaction(query,object : Utils.OnFindTransactionsListener{
            override fun onResults(results: MutableList<Model.Transaction>) {
                transactionVM.updateTransactions(results)
            }
        },transactionVM.service.getTransactionInDb(),null)
    }



    fun addContentListener(){
        imgAddContent.setOnClickListener {
            when(pager.currentItem){
                0 ->{
                    contactVM.updateViewState(ContactState.NEWCONTACT)
                }
                1 ->{
                    issueVM.updateViewState(IssueState.NEWISSUE)
                }
                2 ->{
                    productVM.updateStateView(ProductState.NEWPRODUCT)
                }
                3 ->{
                    transactionVM.updateStateView(TransactionState.NEWTRANSACTION)
                }
            }
        }
    }


    fun openFloatingView(){
        startService(Intent(this, FloatingViewService::class.java))
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        val key = "This is a secret"
        val storage = Storage(applicationContext)
        val files = storage.getNestedFiles(Utils.getPath(this))
        async{
            files.forEach {
                FileEnDecryptManager.getInstance().fileProcessor(Cipher.ENCRYPT_MODE, key,it,it)
            }
        }


    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public fun onTriggerImage(trigger : TriggerImage) {
        val stickyEvent = EventBus.getDefault().getStickyEvent(TriggerImage::class.java)
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent)
        }
        contactVM.updateBase64(trigger.base64)

    }



}
