package com.example.myapplication

import android.app.Application
import com.uds_improveit.preflistener.PrefListener

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PrefListener.apply {
            init(this@MyApplication)
        }
    }
}