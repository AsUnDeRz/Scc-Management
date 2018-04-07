package com.tsongkha.spinnerdatepicker;
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This is a fork of the standard Android DatePicker that additionally allows toggling the year
// on/off.

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple dialog containing an {@link DatePicker}.
 * <p>
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-datepicker.html">Date Picker
 * tutorial</a>.</p>
 */
public class DatePickerDialog extends AlertDialog implements OnClickListener,
        DatePicker.OnDateChangedListener {

    /**
     * Magic year that represents "no year"
     */
    public static int NO_YEAR = DatePicker.NO_YEAR;

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String HOUR ="hour";
    private static final String MINUTE ="minute";
    private static final String YEAR_OPTIONAL = "year_optional";

    private final DatePicker mDatePicker;
    private final OnDateSetListener mCallBack;
    private final DateFormat mTitleDateFormat;
    private final DateFormat mTitleNoYearDateFormat;

    private int mInitialYear;
    private int mInitialMonth;
    private int mInitialDay;
    private int mInitialHour;
    private int mInitialMinute;

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {
        /**
         * @param view        The view associated with this listener.
         * @param year        The year that was set or {@link DatePickerDialog#NO_YEAR} if the user has
         *                    not specified a year
         * @param monthOfYear The month that was set (0-11) for compatibility
         *                    with {@link java.util.Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth,int hour , int minute);
    }

    DatePickerDialog(Context context,
                     int theme,
                     int spinnerTheme,
                     OnDateSetListener callBack,
                     int year,
                     int monthOfYear,
                     int dayOfMonth,
                     int hour,
                     int minute,
                     boolean yearOptional) {
        super(context, theme);

        mCallBack = callBack;
        mInitialYear = year;
        mInitialMonth = monthOfYear;
        mInitialDay = dayOfMonth;
        mInitialHour = hour;
        mInitialMinute = minute;

        mTitleDateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT);
        mTitleNoYearDateFormat = DateUtils.getLocalizedDateFormatWithoutYear(getContext());
        updateTitle(mInitialYear, mInitialMonth, mInitialDay,mInitialHour,mInitialMinute);


        setButton(BUTTON_POSITIVE, context.getText(android.R.string.ok),
                this);
        setButton(BUTTON_NEGATIVE, context.getText(android.R.string.cancel),
                (OnClickListener) null);
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.date_picker_dialog_container, null);
        setView(view);
        mDatePicker = new DatePicker(context, (ViewGroup) view, spinnerTheme);
        mDatePicker.init(mInitialYear, mInitialMonth, mInitialDay,mInitialHour,mInitialMinute, yearOptional, this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int dialogTitle = getContext().getResources().getIdentifier( "alertTitle", "id", "android" );
        Typeface tfs = Typeface.createFromAsset(getContext().getAssets(),"fonts/set_medium.ttf");
        TextView v = findViewById(dialogTitle);
        v.setTypeface(tfs);


    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mCallBack != null) {
            mDatePicker.clearFocus();
            mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
                    mDatePicker.getMonth(), mDatePicker.getDayOfMonth()
                    ,mDatePicker.getHour(),mDatePicker.getMinute());
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth,int hour,int minute) {
        updateTitle(year, monthOfYear, dayOfMonth,hour,minute);
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mInitialYear = year;
        mInitialMonth = monthOfYear;
        mInitialDay = dayOfMonth;
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    private void updateTitle(int year, int month, int day,int hour,int minute) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        //final DateFormat dateFormat = year == NO_YEAR ? mTitleNoYearDateFormat : mTitleDateFormat;
        //final DateFormat dateFormat = DateFormat.getDateInstance(1, new Locale("th","TH"));
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMMM yyyy  HH:mm",new Locale("th","TH"));
        setTitle(dateFormat.format(calendar.getTime())+" น.");
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        state.putInt(HOUR,mDatePicker.getHour());
        state.putInt(MINUTE,mDatePicker.getMinute());
        state.putBoolean(YEAR_OPTIONAL, mDatePicker.isYearOptional());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        boolean yearOptional = savedInstanceState.getBoolean(YEAR_OPTIONAL);
        mDatePicker.init(year, month, day,hour,minute, yearOptional, this);
        updateTitle(year, month, day,hour,minute);
    }

    @Override
    public void show() {
        super.show();
        Typeface tfs = Typeface.createFromAsset(getContext().getAssets(),"fonts/set_medium.ttf");
        getButton(BUTTON_POSITIVE).setTypeface(tfs);
        getButton(BUTTON_NEGATIVE).setTypeface(tfs);
    }
}
