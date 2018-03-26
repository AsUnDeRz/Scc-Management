package asunder.toche.sccmanagement.hover.theme

import org.greenrobot.eventbus.EventBus

/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class HoverThemeManager(private val mBus: EventBus,var  mTheme: HoverTheme) : HoverThemer {

    override fun setTheme(theme: HoverTheme) {
        mTheme = theme
        mBus.postSticky(theme)
    }

    companion object {

        private var sInstance: HoverThemeManager? = null

        @Synchronized
        fun getInstance():HoverThemeManager?{
            if (sInstance == null){
                throw RuntimeException("Cannot obtain HoverThemeManager before calling init().")
            }
            return sInstance
        }

        @Synchronized
        fun init(bus: EventBus, theme: HoverTheme) {
            if (null == sInstance) {
                sInstance = HoverThemeManager(bus, theme)
            }
        }
    }

}
