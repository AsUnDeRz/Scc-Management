package asunder.toche.sccmanagement.hover

import android.content.Context
import android.content.Intent
import android.support.v7.view.ContextThemeWrapper
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.hover.main.ContactHover
import asunder.toche.sccmanagement.hover.main.MainHover
import asunder.toche.sccmanagement.hover.theme.HoverTheme
import io.mattcarroll.hover.HoverView
import io.mattcarroll.hover.window.HoverMenuService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException

/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class HoverService : HoverMenuService() {


    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun getContextForHoverMenu(): Context {
        return ContextThemeWrapper(this, R.style.AppTheme)
    }

    override fun onHoverMenuLaunched(intent: Intent, hoverView: HoverView) {
        mHoverView = hoverView
        mHoverView.setMenu(createHoverMenu())
        mHoverView.collapse()
    }


    fun createHoverMenu(): SccHoverMenu? {
        try {

            mHoverMenu = HoverMenuFactory().createDemoMenuFromCode(contextForHoverMenu, EventBus.getDefault())
            return mHoverMenu
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }



    fun onEventMainThread(newTheme: HoverTheme) {
        mHoverMenu.setTheme(newTheme)
    }

    companion object {

        private val TAG = "DemoHoverMenuService"

        fun showFloatingMenu(context: Context) {
            context.startService(Intent(context, HoverService::class.java))
        }
        lateinit var mHoverView: HoverView
        lateinit var mHoverMenu: SccHoverMenu


        @Subscribe
        fun expenHover(number: String?,name:String) {
            mHoverView.expand()
            //EventBus.getDefault().postSticky(PlaceholderContent.User(number!!,""))
            addHover(number,name)
        }

        fun addHover(number: String?,name: String) {
            mHoverMenu.addSection(ContactHover(mHoverMenu.mContext!!,number!!),number,name)

        }

        fun deleteHover(number: String?){
            mHoverMenu.deleteSection(number!!)
        }


    }
}