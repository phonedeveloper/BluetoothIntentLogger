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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.phonedeveloper.bluetoothintentlogger.intenthandlers.BluetoothHandler;
import com.phonedeveloper.bluetoothintentlogger.intenthandlers.MusicHandler;

/**
 * Receives Broadcast Intents and hands them off to other classes.
 * <p/>
 * To use, install on device and run adb logcat:
 * <p/>
 * $ adb logcat -s BtLogcat
 * <p/>
 * There is no Activity. The app will log received Intents until removed. Remove app by visiting
 * Application settings in the device's Settings menu.
 */
public final class LoggingBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "BluetoothIntentLogger";

    /**
     * Receives Intents broadcast by the Bluetooth subsystem.
     *
     * @param context
     *         The Context in which the receiver is running.
     * @param intent
     *         The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();                 // Get the Intent's action

        // Output a header for this intent log entry, and call the appropriate handler.
        if (action.startsWith("android.bluetooth")) {
            Log.d(TAG, "--------------------New Bluetooth Broadcast Intent-------------------");
            BluetoothHandler.receive(context, intent);
        } else if (action.startsWith("com.android.music")) {
            Log.d(TAG, "--------------------New Android Music Broadcast Intent---------------");
            MusicHandler.receive(intent);
        } else {
            Log.d(TAG, "--------------------Unfamiliar Intent--------------------------------");
            Log.d(TAG, action);
        }
    }
}
