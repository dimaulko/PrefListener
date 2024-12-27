package com.uds_improveit.preflistener

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.uds_improveit.preflistener.Logger.logD

const val KEY_DEVICE_NAME = "DEVICE_NAME"
const val KEY_SERVER_IP = "SERVER_IP"

class SocketConnectionRunnerBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        logD(
            "SocketConnectionRunnerBroadcastReceiver onReceive ${intent?.action} ${intent?.getStringExtra(KEY_DEVICE_NAME)} ${
                intent?.getStringExtra(
                    KEY_SERVER_IP
                )
            }"
        )
        if (intent?.hasExtra(KEY_DEVICE_NAME) == true) {
            PrefListener.connectFromReceiver(
                intent.getStringExtra(KEY_DEVICE_NAME)!!,
                intent.getStringExtra(KEY_SERVER_IP)
            )
        } else {
            logD("can't start socket, missed \"DEVICE_NAME\")} param")
        }
        super.setResult(Activity.RESULT_OK, null /* data */, null /* extra */)
    }
}

