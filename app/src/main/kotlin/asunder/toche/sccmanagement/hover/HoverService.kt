package asunder.toche.sccmanagement.hover

import android.content.Context
import android.content.Intent
import android.support.v7.view.ContextThemeWrapper
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.hover.main.ContactHover
import asunder.toche.sccmanagement.hover.theme.HoverTheme
import asunder.toche.sccmanagement.main.ActivityMain
import asunder.toche.sccmanagement.preference.ROOT
import io.mattcarroll.hover.HoverView
import io.mattcarroll.hover.window.HoverMenuService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException

/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class HoverService : HoverMenuService() {



    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        //EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        //EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun getContextForHoverMenu(): Context {
        return ContextThemeWrapper(this, R.style.AppTheme)
    }

    override fun onHoverMenuLaunched(intent: Intent, hoverView: HoverView) {
        mHoverView = hoverView
        mHoverView.setMenu(createHoverMenu())
        mHoverView.collapse()
        observerHoverView()
    }


    fun createHoverMenu(): SccHoverMenu? {
        try {
            mHoverMenu = HoverMenuFactory().createDemoMenuFromCode(contextForHoverMenu, EventBus.getDefault())
            return mHoverMenu
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    fun observerHoverView(){
        mHoverView.addOnExpandAndCollapseListener(object : HoverView.Listener{
            override fun onExpanding() {
                Log.d("TOCHE","expanding")
                //HoverService.mHoverView.collapse()
            }

            override fun onExpanded() {
                Log.d("TOCHE","expanded")
            }

            override fun onCollapsing() {
                Log.d("TOCHE","collapsing")
            }

            override fun onCollapsed() {
                Log.d("TOCHE","collapsed")
            }

            override fun onClosing() {
            }

            override fun onClosed() {
            }

            override fun onTap() {
                Log.d("TOCHE","onTab")
                currentContact?.let {
                    val intent = Intent()
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(ROOT.CONTACTS, currentContact)
                    applicationContext.startActivity(intent.setClass(this@HoverService, ActivityMain::class.java))
                }
            }
        })
    }



    fun onEventMainThread(newTheme: HoverTheme) {
        mHoverMenu.setTheme(newTheme)
    }

    companion object {

        private val TAG = "HoverService"

        fun showFloatingMenu(context: Context) {
            context.startService(Intent(context, HoverService::class.java))
        }
        lateinit var mHoverView: HoverView
        lateinit var mHoverMenu: SccHoverMenu
        var currentContact: Model.Contact? = null

        fun updateCurrent(contact: Model.Contact){
            currentContact = contact
            println("Update CurrentContact $currentContact")
        }

        @Subscribe
        fun expenHover(number: String?,name:String) {
            mHoverView.expand()
            //EventBus.getDefault().postSticky(PlaceholderContent.User(number!!,""))
            addHover(number,name)
        }

        fun moveToSection(number: String?){

        }

        fun addHover(number: String?,name: String) {
           mHoverMenu.addSection(ContactHover(mHoverMenu.mContext!!,number!!),number,name)
        }

        fun deleteHover(number: String?){
            mHoverMenu.deleteSection(number!!)
        }


    }
}