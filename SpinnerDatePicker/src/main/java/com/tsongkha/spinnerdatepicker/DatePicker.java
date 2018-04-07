package com.tsongkha.spinnerdatepicker;/*
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

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * This is a fork of the standard Android DatePicker that additionally allows toggling the year
 * on/off.
 *
 * A view for selecting a month / year / day based on a calendar like layout.
 *
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-datepicker.html">Date Picker
 * tutorial</a>.</p>
 *
 * For a dialog using this view, see {@link android.app.DatePickerDialog}.
 */
public class DatePicker extends FrameLayout {
    /** Magic year that represents "no year" */
    public static int NO_YEAR = 0;

    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 3000;
    private final int DEFALUT_START_HOUR = 0;
    private final int DEFALUT_END_HOUR = 23;
    private final int DEFALUT_START_MINUTE = 0;
    private final int DEFALUT_END_MINUTE = 59;

    private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();

    /* UI Components */
    private final LinearLayout mPickerContainer;
    private final NumberPicker mDayPicker;
    private final NumberPicker mMonthPicker;
    private final NumberPicker mYearPicker;
    private final NumberPicker mHourPicker;
    private final NumberPicker mMinutePicker;

    /**
     * How we notify users the date has changed.
     */
    private OnDateChangedListener mOnDateChangedListener;

    private int mDay;
    private int mMonth;
    private int mYear;
    private int mHour;
    private int mMinute;
    private boolean mYearOptional;
    private boolean mHasYear;

    /**
     * The callback used to indicate the user changes the date.
     */
    public interface OnDateChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param year The year that was set or {@link DatePicker#NO_YEAR} if no year was set
         * @param monthOfYear The month that was set (0-11) for compatibility
         *  with {@link java.util.Calendar}.
         * @param dayOfMonth The day of the month that was set.
         */
        void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth,int hour,int minute);
    }

    public DatePicker(Context context, ViewGroup root, int numberPickerStyle) {
        super(context, null, 0);

        LayoutInflater inflater = (LayoutInflater) new ContextThemeWrapper(context, numberPickerStyle).getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.date_picker_container, this, true);

        mPickerContainer = findViewById(R.id.parent);

        mDayPicker = inflater.inflate(R.layout.number_picker_day_month, mPickerContainer, true).findViewById(R.id.number_picker);
        mDayPicker.setId(R.id.day);
        mDayPicker.setFormatter(sTwoDigitFormatter);
        mDayPicker.setOnLongPressUpdateInterval(100);
        mDayPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mDay = newVal;
                notifyDateChanged();
            }
        });
        mMonthPicker = inflater.inflate(R.layout.number_picker_day_month, mPickerContainer).findViewById(R.id.number_picker);;
        mMonthPicker.setId(R.id.month);
        mMonthPicker.setFormatter(sTwoDigitFormatter);
        DateFormatSymbols dfs = new DateFormatSymbols(new Locale("th","TH"));
        String[] months = dfs.getShortMonths();

        /*
         * If the user is in a locale where the month names are numeric,
         * use just the number instead of the "month" character for
         * consistency with the other fields.
         */
        if (months[0].startsWith("1")) {
            for (int i = 0; i < months.length; i++) {
                months[i] = String.valueOf(i + 1);
            }
            mMonthPicker.setMinValue(1);
            mMonthPicker.setMaxValue(12);
        } else {
            mMonthPicker.setMinValue(1);
            mMonthPicker.setMaxValue(12);
            mMonthPicker.setDisplayedValues(months);
        }

        mMonthPicker.setOnLongPressUpdateInterval(200);
        mMonthPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                /* We display the month 1-12 but store it 0-11 so always
                 * subtract by one to ensure our internal state is always 0-11
                 */
                mMonth = newVal - 1;
                // Adjust max day of the month
                adjustMaxDay();
                notifyDateChanged();
                updateDaySpinner();
            }
        });
        mYearPicker = inflater.inflate(R.layout.number_picker_year, mPickerContainer).findViewById(R.id.number_picker);
        mYearPicker.setId(R.id.year);
        mYearPicker.setOnLongPressUpdateInterval(100);
        mYearPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mYear = newVal;
                // Adjust max day for leap years if needed
                adjustMaxDay();
                notifyDateChanged();
                updateDaySpinner();
            }
        });
        mYearPicker.setMinValue(DEFAULT_START_YEAR);
        mYearPicker.setMaxValue(DEFAULT_END_YEAR);

        mMinutePicker = inflater.inflate(R.layout.number_picker_day_month, mPickerContainer).findViewById(R.id.number_picker);
        mMinutePicker.setId(R.id.minute);
        mMinutePicker.setMinValue(DEFALUT_START_MINUTE);
        mMinutePicker.setMaxValue(DEFALUT_END_MINUTE);
        mMinutePicker.setOnLongPressUpdateInterval(100);
        mMinutePicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mMinute = newVal;
                notifyDateChanged();
                updateDaySpinner();
            }
        });
        mHourPicker =  inflater.inflate(R.layout.number_picker_day_month, mPickerContainer).findViewById(R.id.number_picker);
        mHourPicker.setId(R.id.hour);
        mHourPicker.setMinValue(DEFALUT_START_HOUR);
        mHourPicker.setMaxValue(DEFALUT_END_HOUR);
        mHourPicker.setOnLongPressUpdateInterval(100);
        mHourPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mHour = newVal;
                notifyDateChanged();
                updateDaySpinner();
            }
        });


        // initialize to current date
        Calendar cal = Calendar.getInstance();
        init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE), null);

        // re-order the number pickers to match the current date format
        reorderPickers();

        mPickerContainer.setLayoutTransition(new LayoutTransition());
        if (!isEnabled()) {
            setEnabled(false);
        }

        root.addView(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mDayPicker.setEnabled(enabled);
        mMonthPicker.setEnabled(enabled);
        mYearPicker.setEnabled(enabled);
        mHourPicker.setEnabled(enabled);
        mMinutePicker.setEnabled(enabled);
    }

    private void reorderPickers() {
        // We use numeric spinners for year and day, but textual months. Ask icu4c what
        // order the user's locale uses for that combination. http://b/7207103.
        String skeleton = mHasYear ? "yyyyMMMdd" : "MMMdd";
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
        char[] order = ICU.getDateFormatOrder(pattern);

        /* Remove the 3 pickers from their parent and then add them back in the
         * required order.
         */
        mPickerContainer.removeAllViews();
        mPickerContainer.addView(mDayPicker);
        mPickerContainer.addView(mMonthPicker);
        mPickerContainer.addView(mYearPicker);
        mPickerContainer.addView(mHourPicker);
        mPickerContainer.addView(mMinutePicker);
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        if (mYear != year || mMonth != monthOfYear || mDay != dayOfMonth) {
            mYear = (mYearOptional && year == NO_YEAR) ? getCurrentYear() : year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateSpinners();
            reorderPickers();
            notifyDateChanged();
        }
    }

    private int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private static class SavedState extends BaseSavedState {

        private final int mYear;
        private final int mMonth;
        private final int mDay;
        private final int mHour;
        private final int mMinute;
        private final boolean mHasYear;
        private final boolean mYearOptional;

        /**
         * Constructor called from {@link DatePicker#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, int year, int month, int day, boolean hasYear,
                           boolean yearOptional,int hour,int minute) {
            super(superState);
            mYear = year;
            mMonth = month;
            mDay = day;
            mHasYear = hasYear;
            mYearOptional = yearOptional;
            mHour = hour;
            mMinute = minute;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mMonth = in.readInt();
            mDay = in.readInt();
            mHour = in.readInt();
            mMinute = in.readInt();
            mHasYear = in.readInt() != 0;
            mYearOptional = in.readInt() != 0;
        }

        public int getYear() {
            return mYear;
        }

        public int getMonth() {
            return mMonth;
        }

        public int getDay() {
            return mDay;
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        public boolean hasYear() {
            return mHasYear;
        }

        public boolean isYearOptional() {
            return mYearOptional;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mYear);
            dest.writeInt(mMonth);
            dest.writeInt(mDay);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
            dest.writeInt(mHasYear ? 1 : 0);
            dest.writeInt(mYearOptional ? 1 : 0);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Creator<SavedState>() {

                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }


    /**
     * Override so we are in complete control of save / restore for this widget.
     */
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        return new SavedState(superState, mYear, mMonth, mDay, mHasYear, mYearOptional,mHour,mMinute);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mYear = ss.getYear();
        mMonth = ss.getMonth();
        mDay = ss.getDay();
        mHasYear = ss.hasYear();
        mYearOptional = ss.isYearOptional();
        updateSpinners();
    }

    /**
     * Initialize the state.
     * @param year The initial year.
     * @param monthOfYear The initial month.
     * @param dayOfMonth The initial day of the month.
     * @param onDateChangedListener How user is notified date is changed by user, can be null.
     */
    public void init(int year, int monthOfYear, int dayOfMonth,int hour,int minute,
                     OnDateChangedListener onDateChangedListener) {
        init(year, monthOfYear, dayOfMonth, hour,minute,false, onDateChangedListener);
    }

    /**
     * Initialize the state.
     * @param year The initial year or {@link #NO_YEAR} if no year has been specified
     * @param monthOfYear The initial month.
     * @param dayOfMonth The initial day of the month.
     * @param yearOptional True if the user can toggle the year
     * @param onDateChangedListener How user is notified date is changed by user, can be null.
     */
    public void init(int year, int monthOfYear, int dayOfMonth,int hour,int minute, boolean yearOptional,
                     OnDateChangedListener onDateChangedListener) {
        mYear = (yearOptional && year == NO_YEAR) ? getCurrentYear() : year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        mHour = hour;
        mMinute = minute;
        mYearOptional = yearOptional;
        mHasYear = yearOptional ? (year != NO_YEAR) : true;
        mOnDateChangedListener = onDateChangedListener;
        updateSpinners();
    }

    private void updateSpinners() {
        updateDaySpinner();
        mYearPicker.setValue(mYear);
        mYearPicker.setVisibility(mHasYear ? View.VISIBLE : View.GONE);

        /* The month display uses 1-12 but our internal state stores it
         * 0-11 so add one when setting the display.
         */
        mMonthPicker.setValue(mMonth + 1);
    }

    private void updateDaySpinner() {
        Calendar cal = Calendar.getInstance();
        // if year was not set, use 2000 as it was a leap year
        cal.set(mHasYear ? mYear : 2000, mMonth, 1);
        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        mDayPicker.setMinValue(1);
        mDayPicker.setMaxValue(max);
        mDayPicker.setValue(mDay);
    }

    public int getYear() {
        return (mYearOptional && !mHasYear) ? NO_YEAR : mYear;
    }

    public boolean isYearOptional() {
        return mYearOptional;
    }

    public int getMonth() {
        return mMonth;
    }

    public int getDayOfMonth() {
        return mDay;
    }

    public int getHour() {
        return mHour;
    }

    public int getMinute() {
        return mMinute;
    }

    private void adjustMaxDay(){
        Calendar cal = Calendar.getInstance();
        // if year was not set, use 2000 as it was a leap year
        cal.set(Calendar.YEAR, mHasYear ? mYear : 2000);
        cal.set(Calendar.MONTH, mMonth);
        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (mDay > max) {
            mDay = max;
        }
    }

    private void notifyDateChanged() {
        if (mOnDateChangedListener != null) {
            int year = (mYearOptional && !mHasYear) ? NO_YEAR : mYear;
            mOnDateChangedListener.onDateChanged(DatePicker.this, year, mMonth, mDay, mHour, mMinute);
        }
    }
}
