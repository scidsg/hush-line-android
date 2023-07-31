package org.scidsg.hushline.android

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HushLineApplication: Application(), Application.ActivityLifecycleCallbacks {

    var isActivityStarted: Boolean = false
        private set

    override fun onCreate() {
        if (BuildConfig.DEBUG) enableStrictMode()
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    private fun isTorProcess(): Boolean {
        val processName = if (Build.VERSION.SDK_INT >= 28) {
            getProcessName()
        } else {
            val pid = Process.myPid()
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            manager.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName ?: return false
        }
        return processName.endsWith(":tor")
    }

    private fun enableStrictMode() {
        val threadPolicy: StrictMode.ThreadPolicy.Builder = StrictMode.ThreadPolicy.Builder().apply {
            detectAll()
            penaltyLog()
            penaltyFlashScreen()
        }
        val vmPolicy = StrictMode.VmPolicy.Builder().apply {
            detectAll()
            penaltyLog()
        }
        StrictMode.setThreadPolicy(threadPolicy.build())
        StrictMode.setVmPolicy(vmPolicy.build())
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is MainActivity) isActivityStarted = true
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity is MainActivity) isActivityStarted = false
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}