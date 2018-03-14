package com.onegravity.contactpicker;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by ToCHe on 11/3/2018 AD.
 */

public class CustomTablayout extends TabLayout {
    public CustomTablayout(Context context) {
        super(context);
    }

    public CustomTablayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTablayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private Float customSize;

    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        super.setupWithViewPager(viewPager);
        Typeface mTypeface = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/set_bold.ttf");
        if (mTypeface != null){
            this.removeAllTabs();
            ViewGroup slidingTabStrip = (ViewGroup) getChildAt(0);
            PagerAdapter adapter = viewPager.getAdapter();
            int i = 0;
            int count = adapter.getCount();
            while (i < count){
                Tab tab = this.newTab();
                this.addTab(tab.setText(adapter.getPageTitle(i)));
                AppCompatTextView view = (AppCompatTextView) ((ViewGroup)slidingTabStrip
                        .getChildAt(i)).getChildAt(1);
                view.setTypeface(mTypeface,Typeface.NORMAL);
                view.setAllCaps(false);
                if (customSize == null) {
                    customSize = (float) getResources().getDimensionPixelSize(R.dimen.txt18);
                }
                view.setTextSize(customSize);

                i++;
            }
        }

    }


    /*
            while (i < count) {
                val tab = this.newTab()
                this.addTab(tab.setText(adapter.getPageTitle(i)))
                val view = (slidingTabStrip.getChildAt(i) as ViewGroup).getChildAt(1) as AppCompatTextView
                view.setTypeface(mTypeface, Typeface.NORMAL)
                view.setAllCaps(false)
                view.textSize = if (customSize == null)
                    resources.getDimensionPixelSize(R.dimen.txt18).toFloat()
                else customSize!!
                i++
                //resources.getDimensionPixelSize(R.dimen.txt18).toFloat()
            }
        }
    }
     */

    void setCustomSize(Float size){
        customSize = size;
    }
}
