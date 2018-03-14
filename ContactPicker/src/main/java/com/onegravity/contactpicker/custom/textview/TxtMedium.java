package com.onegravity.contactpicker.custom.textview;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by ToCHe on 11/3/2018 AD.
 */

class TxtMedium extends AppCompatTextView {

    public TxtMedium(Context context) {
        super(context);
        applyFont(context);
    }

    public TxtMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyFont(context);
    }

    public TxtMedium(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyFont(context);
    }

    private void applyFont(Context context) {
        Typeface customFont = Typeface.createFromAsset(context.getAssets(), "fonts/set_medium.ttf");
        setTypeface(customFont);
    }
}
