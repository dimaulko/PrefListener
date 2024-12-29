package com.uds_improveit.preflistener

import android.content.Context
import android.content.Context.MODE_PRIVATE

const val PREFERENCE_FILE_NAME = " pref_loader"
const val PREF_IP = "PREF_IP"
const val PREF_DEVICE_ID = "PREF_DEVICE_ID"

class PrefUtil(context: Context) {

    private val pref = context.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE)

    var ip: String = ""
        get() {
            return pref.getString(PREF_IP, "")!!
        }
        set(value) {
            pref.edit().putString(PREF_IP, value).apply()
            field = value
        }

    var deviceId: String = ""
        get() {
            return pref.getString(PREF_DEVICE_ID, "")!!
        }
        set(value) {
            pref.edit().putString(PREF_DEVICE_ID, value).apply()
            field = value
        }
}
