package asunder.toche.sccmanagement.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
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
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ManageUserService
import asunder.toche.sccmanagement.settings.ActivitySetting
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_drawer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ActivityMain : AppCompatActivity(), LifecycleOwner {


    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }


    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var actionBar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var contactVM : ContactViewModel
    private val lifecycleRegistry by lazy {
        android.arch.lifecycle.LifecycleRegistry(this)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactVM = ViewModelProviders.of(this).get(ContactViewModel::class.java)
        setContentView(R.layout.activity_main)
        setUpPager()
        setUpTablayout()
        setHumburgerButton()
        observerContacts()
        searchTextChanged()

    }


    fun setUpPager(){
        pager.adapter = MainPager(supportFragmentManager)
        pager.setAllowedSwipeDirection(CustomViewPager.SwipeDirection.none)
        pager.offscreenPageLimit = 3

    }

    fun setUpTablayout(){
        tabLayout.setupWithViewPager(pager)
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
            }
        })
        contactVM.contact.observe(this, Observer {
            txtSearch.setText(it?.company)
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

                    }
                    3 ->{

                    }
                }
            }
        })
    }

}
