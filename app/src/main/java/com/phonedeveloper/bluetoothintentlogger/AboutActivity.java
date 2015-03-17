/*
 * Copyright 2015 PhoneDeveloper LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonedeveloper.bluetoothintentlogger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

/**
 * Provides a launcher Activity that allows the system to send Intents to our BroadcastReceiver.
 */
public class AboutActivity extends Activity implements View.OnClickListener {

    CheckBox verboseCheckBox;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        verboseCheckBox = (CheckBox) findViewById(R.id.verboseCheckBox);
        verboseCheckBox.setOnClickListener(this);

        prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean verbose = prefs.getBoolean(getString(R.string.preference_verbosity_key), false);
        verboseCheckBox.setChecked(verbose);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v
     *         The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.preference_verbosity_key), ((CheckBox)v).isChecked());
        editor.apply();
    }
}
