package asunder.toche.sccmanagement.hover

import android.content.Context
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.hover.theme.HoverTheme
import asunder.toche.sccmanagement.hover.ui.TabView
import asunder.toche.sccmanagement.preference.ROOT
import io.mattcarroll.hover.Content
import io.mattcarroll.hover.HoverMenu
import java.io.IOException
import java.util.ArrayList

/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class SccHoverMenu @Throws(IOException::class)
constructor(val mContext: Context?,
            private val mMenuId: String,
            data: Map<String, Content>,
            private var mTheme: HoverTheme?) : HoverMenu() {
    private val mSections : MutableList<Section> = mutableListOf()
    private var limitSection = 5

    init {
        for (tabId in data.keys) {
            mSections.add(HoverMenu.Section(
                    HoverMenu.SectionId(tabId),
                    createTabView(tabId),
                    data[tabId]!!
            ))
        }
        setSection()
    }

    fun addSection(content: Content,sectionId: String,name:String){
        var hasSectionId = false
        mSections.forEach {
            if(it.id == SectionId(sectionId)){
                hasSectionId = true
            }
        }

        if (!hasSectionId) {
            if (mSections.size == limitSection) {
                mSections.removeAt(1)
            }
            mSections.add(0,HoverMenu.Section(
                    HoverMenu.SectionId(sectionId),
                    createTabViewWithTitle(name.substring(0,1).toUpperCase(),mTheme!!.baseColor, mTheme!!.accentColor),
                    content
            ))
            notifyMenuChanged()
        }
    }

    fun deleteSection(sectionId: String){
        System.out.println("Prepare delete Section $sectionId")
        for (i in 0 until mSections.lastIndex){
            if (mSections[i].id == SectionId(sectionId)){
                mSections.removeAt(i)
            }
        }
        notifyMenuChanged()

    }

    fun setTheme(theme: HoverTheme) {
        mTheme = theme
        notifyMenuChanged()
    }

    private fun createTabView(sectionId: String): View {
        return when {
            MAIN_HOVER == sectionId -> createTabView(R.drawable.ic_record_voice_over_white_24dp,mTheme!!.baseColor, mTheme!!.accentColor)
            else -> createTabView(R.drawable.ic_contact_phone_white_36dp,mTheme!!.baseColor, mTheme!!.accentColor)
        }
    }

    private fun createTabView(@DrawableRes tabBitmapRes: Int, @ColorInt backgroundColor: Int, @ColorInt iconColor: Int?): View {
        val resources = mContext?.resources
        val elevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources?.displayMetrics)
        val view = TabView(mContext!!, ContextCompat.getDrawable(mContext,R.drawable.tab_background)!!,ContextCompat.getDrawable(mContext,tabBitmapRes))
        view.setTabBackgroundColor(backgroundColor)
        view.setTabForegroundColor(iconColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.elevation = elevation
        }
        return view
    }

    private fun createTabViewWithTitle(title:String, @ColorInt backgroundColor: Int, @ColorInt iconColor: Int?): View {
        val resources = mContext?.resources
        val elevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources?.displayMetrics)
        val view = TabView(mContext!!, ContextCompat.getDrawable(mContext,R.drawable.tab_background)!!,title)
        view.setTabBackgroundColor(backgroundColor)
        view.setTabForegroundColor(iconColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.elevation = elevation
        }
        return view
    }


    override fun getId(): String {
        return mMenuId
    }

    override fun getSectionCount(): Int {
        return mSections.size
    }

    override fun getSection(index: Int): HoverMenu.Section? {
        System.out.println("GetSection With index   $index")
        return mSections[index]
    }

    override fun getSection(sectionId: HoverMenu.SectionId): HoverMenu.Section? {
        for (section in mSections) {
            if (section.id == sectionId) {
                return section
            }
        }
        return null
    }



    override fun getSections(): List<HoverMenu.Section> {
        return mSections.toList()
    }

    companion object {

        val INTRO_ID = "intro"
        val SELECT_COLOR_ID = "select_color"
        val APP_STATE_ID = "app_state"
        val MAIN_HOVER = "main_hover"
        val MENU_ID = "menu"
        val PLACEHOLDER_ID = "placeholder"
    }
}
