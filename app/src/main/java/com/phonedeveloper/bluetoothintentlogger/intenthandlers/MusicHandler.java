/*
 * Copyright (C) 2015 PhoneDeveloper LLC
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

package com.phonedeveloper.bluetoothintentlogger.intenthandlers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.phonedeveloper.bluetoothintentlogger.LoggingBroadcastReceiver;

/**
 * Handles Broadcast Intents from the music player.
 */
public class MusicHandler {

    private static final String TAG = LoggingBroadcastReceiver.TAG;

    /**
     * Handles a music player Broadcast Intent.
     *
     * @param intent
     *         the Intent as provided to the original BroadcastReceiver's onReceive() method.
     */
    public static void receive(Intent intent) {

        String action = intent.getAction();                     // Get the Intent's action

        // Log the action name.
        Log.d(TAG, "Action: " + action);

        //
        // Find and output any Extras.
        //

        // Get all the keys and values contained in extras.
        Bundle extras = intent.getExtras();                     // Get all the Intent's extras
        if (extras != null) {
            // We'll output an additional log line for each key.
            for (String key : extras.keySet()) {

                // Get the value associated with this key, and its data type.
                Object extra = extras.get(key);                     // Get the key's value
                if (extra != null) {
                    String extraType = extra.getClass().getName();  // Get the datatype of the value

                    // Convert extras of various data types to a printable String.
                    String extraValue;
                    switch (extraType) {
                        case "java.lang.String":
                            extraValue = "\"" + extra + "\"";
                            break;
                        case "java.lang.Integer":
                            extraValue = extra.toString();
                            break;
                        case "java.lang.Boolean":
                            if ((Boolean) extra) {
                                extraValue = "true";
                            } else {
                                extraValue = "false";
                            }
                            break;
                        case "java.lang.Long":
                            extraValue = extra.toString();
                            break;
                        default:
                            extraValue = "(not parsed, type = " + extraType + ")";
                            break;
                    }

                    Log.d(TAG, "  Extra: \"" + key + "\"" +
                            "   Type: " + extraType +
                            "   Value: " + extraValue);
                }
            }
        }
    }
}
