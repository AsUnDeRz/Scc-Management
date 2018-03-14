package asunder.toche.sccmanagement.main

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.auth.ActivityLogin
import asunder.toche.sccmanagement.auth.ActivityManagement
import asunder.toche.sccmanagement.custom.TriggerUpdate
import asunder.toche.sccmanagement.custom.pager.CustomViewPager
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.service.ManageUserService
import asunder.toche.sccmanagement.settings.ActivitySetting
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_drawer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ActivityMain : AppCompatActivity() {

    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var actionBar: Toolbar
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpPager()
        setUpTablayout()
        setHumburgerButton()

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

}
