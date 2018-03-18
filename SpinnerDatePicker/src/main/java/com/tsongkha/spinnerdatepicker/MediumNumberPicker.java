package com.tsongkha.spinnerdatepicker;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by ToCHe on 16/3/2018 AD.
 */

public class MediumNumberPicker extends NumberPicker {



    public MediumNumberPicker(Context context) {
        super(context);
    }

    public MediumNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediumNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
    override fun updateViewLayout(view: View?, params: ViewGroup.LayoutParams?) {
        super.updateViewLayout(view, params)
        val tfs = Typeface.createFromAsset(context.assets, "fonts/set_medium.ttf")
        if (view is EditText) {
            view.typeface = tfs
            //view.textSize = 25f
        }
    }

     */

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        updateView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    void updateView(View view){
        Typeface  tfs = Typeface.createFromAsset(view.getContext().getAssets(),"fonts/set_medium.ttf");
        if (view instanceof EditText) {
            ((EditText) view).setTypeface(tfs);
        }
    }
}
