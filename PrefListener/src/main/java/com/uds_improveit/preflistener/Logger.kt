package com.uds_improveit.preflistener

import android.util.Log

object Logger {

    private const val TAG = "PrefListener"

    fun logD(message: String) {
        if (PrefListener.isDebuggable && development) {
            Log.d(TAG, message)
        }
    }

    fun logW(message: String) {
        if (PrefListener.isDebuggable) {
            Log.w(TAG, message)
        }
    }

}