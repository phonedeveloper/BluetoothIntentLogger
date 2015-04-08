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

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.phonedeveloper.bluetoothintentlogger.LoggingBroadcastReceiver;
import com.phonedeveloper.bluetoothintentlogger.R;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Handles Bluetooth Intents.
 */
public class BluetoothHandler {

    private static final String TAG = LoggingBroadcastReceiver.TAG;

    /**
     * A table of Intent action prefixes, and the Bluetooth classes they are associated with.
     * <p/>
     * This table is provided so that the logging code can identify the name of the Bluetooth API
     * class in which the action is documented, for readability.
     */
    private static final String[][] sClassNames =
            {
                    {
                            "android.bluetooth.a2dp.profile.action.",
                            "BluetoothA2dp"
                    },
                    {
                            "android.bluetooth.adapter.action.",
                            "BluetoothAdapter"
                    },
                    {
                            "android.bluetooth.device.action.",
                            "BluetoothDevice"
                    },
                    {
                            "android.bluetooth.headset.profile.action.",
                            "BluetoothHeadset"
                    }
            };

    /**
     * A table of all prefixes of Bluetooth Extra key Strings that can be encountered.
     * <p/>
     * This table is provided so that the logging code can replace these substrings with the word
     * "EXTRA_" to make the log output resemble the names of the Extras as found in the Bluetooth
     * API.
     */
    private static final String[] sExtraPrefixes =
            {
                    "android.bluetooth.adapter.extra.",
                    "android.bluetooth.device.extra.",
                    "android.bluetooth.profile.extra.",
                    "android.bluetooth.headset.extra."
            };

    /**
     * Provides the constant name for an Extra value, as found in the Bluetooth APIs.
     * <p/>
     * For log readability, we want to show the name of a constant as found in the Bluetooth API
     * rather than the constant itself.
     * <p/>
     * For example, it's better to show extra states "STATE_DISCONNECTED" or "STATE_CONNECTED" than
     * to show 0 or 2. But when we read an intent's extras, we get the actual constant value (0 or
     * 2), not the API's constant name (STATE_DISCONNECTED or STATE_CONNECTED).
     * <p/>
     * To get the API's name given a value, we need a lookup table. But since values are reused
     * throughout the API, we need more than the value to get the constant name. We also need the
     * action name and the extra key.
     * <p/>
     * To create a unique key for each API constant name, we concatenate the action name, extra key,
     * and constant value into one long String.
     * <p/>
     * This table holds all the possible key-value pairs that uniquely provide an API constant name
     * given an action, extra key, and extra value.
     */
    private static final String[][] sExtraConstantNames =
            {
                    /*
                     * BluetoothA2dp action ACTION_CONNECTION_STATE_CHANGED
                     *               extra  EXTRA_STATE
                     */
                    {
                            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "0",
                            "STATE_DISCONNECTED"
                    },
                    {
                            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "1",
                            "STATE_CONNECTING"
                    },
                    {
                            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "2",
                            "STATE_CONNECTED"
                    },
                    {
                            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "3",
                            "STATE_DISCONNECTING"
                    },

                    /*
                     * BluetoothA2dp action ACTION_CONNECTION_STATE_CHANGED
                     *               extra  EXTRA_PREVIOUS_STATE
                     */
                    {
                            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "0",
                            "STATE_DISCONNECTED"
                    },
                    {
                            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "1",
                            "STATE_CONNECTING"
                    },
                    {
                            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "2",
                            "STATE_CONNECTED"
                    },
                    {
                            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "3",
                            "STATE_DISCONNECTING"
                    },

                    /*
                     * BluetoothA2dp action ACTION_PLAYING_STATE_CHANGED
                     *               extra  EXTRA_STATE
                     */
                    {
                            "android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "10",
                            "STATE_NOT_PLAYING"
                    },
                    {
                            "android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "11",
                            "STATE_PLAYING"
                    },

                    /*
                     * BluetoothA2dp action ACTION_PLAYING_STATE_CHANGED
                     *               extra  EXTRA_PREVIOUS_STATE
                     */
                    {
                            "android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "10",
                            "STATE_NOT_PLAYING"
                    },
                    {
                            "android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "11",
                            "STATE_PLAYING"
                    },

                    /*
                     * BluetoothAdapter action ACTION_CONNECTION_STATE_CHANGED,
                     *                  extra  EXTRA_CONNECTION_STATE
                     */
                    {
                            "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.CONNECTION_STATE" +
                                    "0",
                            "STATE_DISCONNECTED"
                    },
                    {
                            "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.CONNECTION_STATE" +
                                    "1",
                            "STATE_CONNECTING"
                    },
                    {
                            "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.CONNECTION_STATE" +
                                    "2",
                            "STATE_CONNECTED"
                    },
                    {
                            "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.CONNECTION_STATE" +
                                    "3",
                            "STATE_DISCONNECTING"
                    },

                    /*
                     * BluetoothAdapter action ACTION_CONNECTION_STATE_CHANGED,
                     *                  extra  EXTRA_PREVIOUS_CONNECTION_STATE
                     */
                    {
                            "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_CONNECTION_STATE" +
                                    "0",
                            "STATE_DISCONNECTED"
                    },
                    {
                            "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_CONNECTION_STATE" +
                                    "1",
                            "STATE_CONNECTING"
                    },
                    {
                            "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_CONNECTION_STATE" +
                                    "2",
                            "STATE_CONNECTED"
                    },
                    {
                            "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_CONNECTION_STATE" +
                                    "3",
                            "STATE_DISCONNECTING"
                    },

                    /*
                     * BluetoothAdapter action ACTION_SCAN_MODE_CHANGED,
                     *                  extra  EXTRA_SCAN_MODE
                     */
                    {
                            "android.bluetooth.adapter.action.SCAN_MODE_CHANGED" +
                                    "android.bluetooth.adapter.extra.SCAN_MODE" +
                                    "20",
                            "SCAN_MODE_NONE"
                    },
                    {
                            "android.bluetooth.adapter.action.SCAN_MODE_CHANGED" +
                                    "android.bluetooth.adapter.extra.SCAN_MODE" +
                                    "21",
                            "SCAN_MODE_CONNECTABLE"
                    },
                    {
                            "android.bluetooth.adapter.action.SCAN_MODE_CHANGED" +
                                    "android.bluetooth.adapter.extra.SCAN_MODE" +
                                    "23",
                            "SCAN_MODE_CONNECTABLE_DISCOVERABLE"
                    },

                    /*
                     * BluetoothAdapter action ACTION_SCAN_MODE_CHANGED,
                     *                  extra  EXTRA_PREVIOUS_SCAN_MODE
                     */
                    {
                            "android.bluetooth.adapter.action.SCAN_MODE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_SCAN_MODE" +
                                    "20",
                            "SCAN_MODE_NONE"
                    },
                    {
                            "android.bluetooth.adapter.action.SCAN_MODE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_SCAN_MODE" +
                                    "21",
                            "SCAN_MODE_CONNECTABLE"
                    },
                    {
                            "android.bluetooth.adapter.action.SCAN_MODE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_SCAN_MODE" +
                                    "23",
                            "SCAN_MODE_CONNECTABLE_DISCOVERABLE"
                    },

                    /*
                     * BluetoothAdapter action ACTION_STATE_CHANGED,
                     *                  extra  EXTRA_STATE
                     */
                    {
                            "android.bluetooth.adapter.action.STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.STATE" +
                                    "10",
                            "STATE_OFF"
                    },
                    {
                            "android.bluetooth.adapter.action.STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.STATE" +
                                    "11",
                            "STATE_TURNING_ON"
                    },
                    {
                            "android.bluetooth.adapter.action.STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.STATE" +
                                    "12",
                            "STATE_ON"
                    },
                    {
                            "android.bluetooth.adapter.action.STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.STATE" +
                                    "13",
                            "STATE_TURNING_OFF"
                    },

                    /*
                     * BluetoothAdapter action ACTION_STATE_CHANGED,
                     *                  extra  EXTRA_PREVIOUS_STATE
                     */
                    {
                            "android.bluetooth.adapter.action.STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_STATE" +
                                    "10",
                            "STATE_OFF"
                    },
                    {
                            "android.bluetooth.adapter.action.STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_STATE" +
                                    "11",
                            "STATE_TURNING_ON"
                    },
                    {
                            "android.bluetooth.adapter.action.STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_STATE" +
                                    "12",
                            "STATE_ON"
                    },
                    {
                            "android.bluetooth.adapter.action.STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_STATE" +
                                    "13",
                            "STATE_TURNING_OFF"
                    },

                    /*
                     * BluetoothDevice action ACTION_BOND_STATE_CHANGED,
                     *                 extra  EXTRA_BOND_STATE
                     */
                    {
                            "android.bluetooth.adapter.action.BOND_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.BOND_STATE" +
                                    "10",
                            "BOND_NONE"
                    },
                    {
                            "android.bluetooth.adapter.action.BOND_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.BOND_STATE" +
                                    "11",
                            "BOND_BONDING"
                    },
                    {
                            "android.bluetooth.adapter.action.BOND_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.BOND_STATE" +
                                    "12",
                            "BOND_BONDED"
                    },

                    /*
                     * BluetoothDevice action ACTION_BOND_STATE_CHANGED,
                     *                  extra  EXTRA_PREVIOUS_BOND_STATE
                     */
                    {
                            "android.bluetooth.adapter.action.BOND_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_BOND_STATE" +
                                    "10",
                            "BOND_NONE"
                    },
                    {
                            "android.bluetooth.adapter.action.BOND_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_BOND_STATE" +
                                    "11",
                            "BOND_BONDING"
                    },
                    {
                            "android.bluetooth.adapter.action.BOND_STATE_CHANGED" +
                                    "android.bluetooth.adapter.extra.PREVIOUS_BOND_STATE" +
                                    "12",
                            "BOND_BONDED"
                    },

                    /*
                     * BluetoothHeadset action ACTION_AUDIO_STATE_CHANGED
                     *                  extra  EXTRA_STATE
                     */
                    {
                            "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "10",
                            "STATE_AUDIO_DISCONNECTED"
                    },
                    {
                            "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "11",
                            "STATE_AUDIO_CONNECTING"
                    },
                    {
                            "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "12",
                            "STATE_AUDIO_CONNECTED"
                    },

                    /*
                     * BluetoothHeadset action ACTION_AUDIO_STATE_CHANGED
                     *                  extra  EXTRA_PREVIOUS_STATE
                     */
                    {
                            "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "10",
                            "STATE_AUDIO_DISCONNECTED"
                    },
                    {
                            "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "11",
                            "STATE_AUDIO_CONNECTING"
                    },
                    {
                            "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "12",
                            "STATE_AUDIO_CONNECTED"
                    },

                    /*
                     * BluetoothHeadset action ACTION_CONNECTION_STATE_CHANGED
                     *                  extra  EXTRA_STATE
                     */
                    {
                            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "0",
                            "STATE_DISCONNECTED"
                    },
                    {
                            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "1",
                            "STATE_CONNECTING"
                    },
                    {
                            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "2",
                            "STATE_CONNECTED"
                    },
                    {
                            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.STATE" +
                                    "3",
                            "STATE_DISCONNECTING"
                    },

                    /*
                     * BluetoothHeadset action ACTION_CONNECTION_STATE_CHANGED
                     *                  extra  EXTRA_PREVIOUS_STATE
                     */
                    {
                            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "0",
                            "STATE_DISCONNECTED"
                    },
                    {
                            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "1",
                            "STATE_CONNECTING"
                    },
                    {
                            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "2",
                            "STATE_CONNECTED"
                    },
                    {
                            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" +
                                    "android.bluetooth.profile.extra.PREVIOUS_STATE" +
                                    "3",
                            "STATE_DISCONNECTING"
                    },

                    /*
                     * BluetoothHeadset action ACTION_VENDOR_SPECIFIC_HEADSET_EVENT
                     *                  extra  EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE
                     */
                    {
                            "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT" +
                                    "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE" +
                                    "0",
                            "AT_CMD_TYPE_READ"
                    },
                    {
                            "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT" +
                                    "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE" +
                                    "1",
                            "AT_CMD_TYPE_TEST"
                    },
                    {
                            "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT" +
                                    "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE" +
                                    "2",
                            "AT_CMD_TYPE_SET"
                    },
                    {
                            "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT" +
                                    "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE" +
                                    "3",
                            "AT_CMD_TYPE_BASIC"
                    },
                    {
                            "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT" +
                                    "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE" +
                                    "4",
                            "AT_CMD_TYPE_ACTION"
                    }
            };

    /**
     * A HashMap which provides the Bluetooth API constant name for the value of an Extra, given the
     * Intent's action and Extra key.
     */
    private static final HashMap<String, String> sExtraLookupMap;

    /**
     * A HashMap which provides the major names of each device type (from
     * BluetoothClass.Device.Major) given its numeric value.
     */
    private static final HashMap<Integer, String> sDeviceClassMajorLookupMap;

    /**
     * A HashMap which provides the name of each device type (from BluetoothClass.Device)
     * given its numeric value.
     */
    private static final HashMap<Integer, String> sDeviceClassMinorLookupMap;

    /**
     * A HashMap which provides the names for various device extras.
     */
    private static final HashMap<String, String> sDeviceExtraNameLookupMap;

    static {
        sExtraLookupMap = new HashMap<>();

        // Add each constant key/value to the HashMap.
        for (String[] constantName : sExtraConstantNames) {
            sExtraLookupMap.put(constantName[0], constantName[1]);
        }

        sDeviceClassMajorLookupMap = new HashMap<>();

        // Get the names for each of the Bluetooth device classes.
        for (Field field : BluetoothClass.Device.Major.class.getFields()) {
            try {
                int deviceClassMajor = field.getInt(null);
                sDeviceClassMajorLookupMap.put(deviceClassMajor, field.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        sDeviceClassMinorLookupMap = new HashMap<>();

        // Get the names for each of the Bluetooth device classes.
        for (Field field : BluetoothClass.Device.class.getFields()) {
            try {
                int deviceClass = field.getInt(null);
                sDeviceClassMinorLookupMap.put(deviceClass, field.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        sDeviceExtraNameLookupMap = new HashMap<>();

        // Get the names for each of the Bluetooth device classes.
        for (Field field : BluetoothDevice.class.getFields()) {
            try {
                Object fieldValue = field.get(null);
                if (fieldValue.getClass().getName().equals("java.lang.String")) {
                    if (field.getName().startsWith("EXTRA")) {
                        String deviceExtraValue = (String) field.get(null);
                        sDeviceExtraNameLookupMap.put(deviceExtraValue, field.getName());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles a Bluetooth Broadcast Intent.
     *
     * @param intent
     *         the Intent as provided to the original BroadcastReceiver's onReceive() method.
     */
    public static void receive(Context context, Intent intent) {

        String action = intent.getAction();                     // Get the Intent's action

        //
        // Determine whether we are logging verbosely or not
        //
        boolean verbose;
        SharedPreferences prefs =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        Context.MODE_PRIVATE);
        verbose = prefs.getBoolean(context.getString(R.string.preference_verbosity_key), false);

        //
        // See if the action is found in our table of classes, so we can output a class name
        // that can be found in the API guides.
        //
        String className = "(unknown class)";
        String actionName = action;
        for (String[] name : sClassNames) {
            if (action.startsWith(name[0])) {                  // found matching package name?
                className = name[1];                           // then get class and action names
                actionName = "ACTION_" + action.substring(name[0].length(), action.length());
                break;
            }
        }

        //
        // Log the action.
        //
        Log.d(TAG, className + "." + actionName);
        if (verbose) {
            Log.d(TAG, "Action: " + action);
        }

        //
        // Now, find and and parse any Extras.
        //

        // Get all the keys and values contained in extras.
        Bundle extras = intent.getExtras();                        // Get all the Intent's extras
        if (extras == null) {
            Log.d(TAG, "...Intent has no extras.");
        } else {

            //
            // Output a new log line for each extra.
            //
            for (String key : extras.keySet()) {

                String valueString = null;
                String type = null;

                //
                // Get the value associated with this key, and its data type.
                //
                Object value = extras.get(key);              // Get the value for this key
                if (value != null) {                         // value provided?
                    type = value.getClass().getName();       // Get the type of the value

                    // Use the action, key and value strings to try to find the extra's value
                    // as described in the Bluetooth API documentation. How we do this depends on
                    // whether the extra is a String, Integer, etc.
                    String lookupKey = action + key + value;

                    // Convert each type of Extra to a printable String.
                    switch (type) {
                        case "java.lang.String":
                            if (sExtraLookupMap.containsKey(lookupKey)) {
                                valueString = sExtraLookupMap.get(lookupKey);
                            } else {
                                value = "\"" + value + "\"";
                            }
                            break;
                        case "java.lang.Integer":
                            if (sExtraLookupMap.containsKey(lookupKey)) {
                                valueString = sExtraLookupMap.get(lookupKey);
                            }
                            break;
                        case "java.lang.Short":
                            // just output value
                            break;
                        case "android.bluetooth.BluetoothDevice":
                            // Display the friendly name, device type, and address
                            BluetoothDevice device = (BluetoothDevice) value;
                            //
                            // Get Bluetooth name
                            //
                            if (device.getName() != null) {
                                valueString = "\"" + device.getName() + "\"";
                            } else {
                                // If Bluetooth is disconnected, the device name won't be available.
                                valueString = "null";
                            }
                            //
                            // Get Bluetooth device type
                            //
                            if (android.os.Build.VERSION.SDK_INT >= 18) {       // getType() is API 18+
                                switch (device.getType()) {
                                    // TODO Extract these using reflection.
                                    case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                                        valueString += "/DEVICE_TYPE_CLASSIC/";
                                        break;
                                    case BluetoothDevice.DEVICE_TYPE_LE:
                                        valueString += "/DEVICE_TYPE_LE/";
                                        break;
                                    case BluetoothDevice.DEVICE_TYPE_DUAL:
                                        valueString += "/DEVICE_TYPE_DUAL/";
                                        break;
                                    default:
                                        // Device type not available if Bluetooth disconnected.
                                        valueString += "/unavailable/";
                                }
                            }
                            //
                            // Get Bluetooth address
                            //
                            if (device.getAddress() != null) {
                                valueString += device.getAddress();
                            } else {
                                valueString += "unavailable";
                            }
                            break;
                        case "android.bluetooth.BluetoothClass":
                            // Display the name of this device's class major and device type
                            BluetoothClass deviceClass = (BluetoothClass) value;
                            int majorDeviceClassValue = deviceClass.getMajorDeviceClass();
                            int deviceClassValue = deviceClass.getDeviceClass();
                            if (sDeviceClassMajorLookupMap.containsKey(majorDeviceClassValue)) {
                                valueString = sDeviceClassMajorLookupMap.get(majorDeviceClassValue);
                                if (sDeviceClassMinorLookupMap.containsKey(deviceClassValue)) {
                                    valueString += "/"
                                            + sDeviceClassMinorLookupMap.get(deviceClassValue);
                                } else {
                                    valueString += "/unrecognized";
                                }
                            } else {
                                valueString = "unrecognized";
                                if (sDeviceClassMinorLookupMap.containsKey(deviceClassValue)) {
                                    valueString += "/"
                                            + sDeviceClassMinorLookupMap.get(deviceClassValue);
                                } else {
                                    valueString += "/unrecognized";
                                }
                            }
                            break;
                        default:
                            valueString = "(not parsed)";
                            value = "";
                            break;
                    }
                }

                //
                // Rewrite the extra key so it appears as found in Android API documentation.
                //.
                for (String prefix : sExtraPrefixes) {       // go through known extra prefixes
                    if (key.startsWith(prefix)) {            // found matching prefix?
                        /*
                         * TODO maybe we need a database of non-AOSP extras.
                         * For example, we should be able to flag GEARMANAGER_NAME as
                         * a Samsung extra.
                         */
                        if (sDeviceExtraNameLookupMap.containsKey(key)) {
                            if (!verbose) {
                                key = sDeviceExtraNameLookupMap.get(key);
                            } else {
                                key = sDeviceExtraNameLookupMap.get(key) + " (" + key + ")";
                            }
                        }
                        break;                               // yes, rewrite key, done
                    }
                }

                //
                // Prepare formatting based on user's desired verbosity
                //
                String keyPrefix, valueStringPrefix, valuePrefix, typePrefix;
                String keySuffix, valueStringSuffix, valueSuffix, typeSuffix;

                if (verbose) {
                    keyPrefix = "Extra: ";
                    keySuffix = "   ";

                    if (valueString != null) {
                        switch (type) {
                            case "android.bluetooth.BluetoothDevice":
                                // Value String format
                                if (android.os.Build.VERSION.SDK_INT >= 18) {
                                    valueStringPrefix = "Device Name/Type/Address: ";
                                } else {
                                    valueStringPrefix = "Device Name/Address: ";
                                }
                                valueStringSuffix = "   ";

                                // Value format
                                valuePrefix = "";
                                value = "";
                                valueSuffix = "";
                                break;
                            case "android.bluetooth.BluetoothClass":
                                // Value String format
                                valueStringPrefix = "Device Major/Class: ";
                                valueStringSuffix = "   ";

                                // Value format
                                valuePrefix = "";
                                value = "";
                                valueSuffix = "";
                                break;
                            default:
                                // Value String format
                                valueStringPrefix = "Value: ";
                                valueStringSuffix = " ";

                                // Value format
                                if (valueString.equals("(not parsed)")) {
                                    valuePrefix = "";
                                    value = "";
                                    valueSuffix = "";
                                } else {
                                    valuePrefix = "(";
                                    valueSuffix = ")   ";
                                }
                                break;
                        }
                    } else {            // no valueString, only a value
                        // Value String format
                        valueStringPrefix = "";
                        valueString = "";
                        valueStringSuffix = "";

                        // Value format
                        if (value != null) {
                            valuePrefix = "Value: ";
                            valueSuffix = "   ";
                        } else {
                            valuePrefix = "";
                            value = "";
                            valueSuffix = "";
                        }
                    }

                    if (type != null) {
                        typePrefix = "Type: ";
                        typeSuffix = "";
                    } else {
                        typePrefix = "";
                        type = "";
                        typeSuffix = "";
                    }
                } else {
                    keyPrefix = "";
                    keySuffix = " ";

                    // Show either valueString or value but not both
                    if (valueString == null) {
                        valueString = "";
                    } else {
                        value = "";
                    }

                    valueStringPrefix = "";
                    valueStringSuffix = "";

                    valuePrefix = "";
                    if (value == null) value = "";
                    valueSuffix = "";

                    // Don't show data type
                    typePrefix = "";
                    type = "";
                    typeSuffix = "";
                }

                //
                // Log it!
                //
                Log.d(TAG, String.format("%s%s%s%s%s%s%s%s%s%s%s%s",
                        keyPrefix, key, keySuffix,
                        valueStringPrefix, valueString, valueStringSuffix,
                        valuePrefix, value, valueSuffix,
                        typePrefix, type, typeSuffix));
            }
        }
    }
}
