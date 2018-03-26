package asunder.toche.sccmanagement.hover

import android.content.Context
import asunder.toche.sccmanagement.hover.main.MainHover
import asunder.toche.sccmanagement.hover.theme.HoverThemeManager
import asunder.toche.sccmanagement.preference.ROOT
import io.mattcarroll.hover.Content
import io.mattcarroll.hover.content.menus.*
import io.mattcarroll.hover.content.toolbar.ToolbarNavigator
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.*

/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class HoverMenuFactory {

    /**
     * Example of how to create a menu in code.
     * @return HoverMenu
     */
    @Throws(IOException::class)
    fun createDemoMenuFromCode(context: Context, bus: EventBus): SccHoverMenu {
        val menu = LinkedHashMap<String, Content>()
        menu[SccHoverMenu.MAIN_HOVER] = MainHover(context)
        menu[SccHoverMenu.MAIN_HOVER] = MainHover(context)
        menu[SccHoverMenu.MAIN_HOVER] = MainHover(context)
        menu[SccHoverMenu.MAIN_HOVER] = MainHover(context)
        menu[SccHoverMenu.MAIN_HOVER] = MainHover(context)
        return SccHoverMenu(context, ROOT.HOVER, menu, HoverThemeManager.getInstance()?.mTheme)
    }

}
