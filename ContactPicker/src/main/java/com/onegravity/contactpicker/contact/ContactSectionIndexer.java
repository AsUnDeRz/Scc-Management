/*
 * Copyright (C) 2015-2017 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onegravity.contactpicker.contact;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;

public class ContactSectionIndexer extends SectionTitleIndicator<ContactSection> {

    public ContactSectionIndexer(Context context) {
        super(context);
        appleFont(context);
    }

    public ContactSectionIndexer(Context context, AttributeSet attrs) {
        super(context, attrs);
        appleFont(context);
    }

    public ContactSectionIndexer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        appleFont(context);
    }

    @Override
    public void setSection(ContactSection section) {
        setTitleText( section.getLetter() + "");
    }
    void appleFont(Context context){
        TextView t = getRootView().findViewById(xyz.danoz.recyclerviewfastscroller.R.id.section_indicator_text);
        Typeface customFont = Typeface.createFromAsset(context.getAssets(), "fonts/set_bold.ttf");
        t.setTypeface(customFont);
    }



}
