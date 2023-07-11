package com.kdroid.blescanapp.application

import android.app.Application
import com.kdroid.blescanapp.BuildConfig
import com.kdroid.blescanapp.utils.ClickableLineNumberDebugTree
import timber.log.Timber

class BleScanApplication : Application() {


    companion object {
        lateinit var instance: BleScanApplication
        fun get(): BleScanApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (BuildConfig.DEBUG) {
            Timber.plant(ClickableLineNumberDebugTree())
        }
    }
}