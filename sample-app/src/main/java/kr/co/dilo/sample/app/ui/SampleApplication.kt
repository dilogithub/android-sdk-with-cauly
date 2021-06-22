package kr.co.dilo.sample.app.ui

import android.app.Application
import androidx.preference.PreferenceManager
import kr.co.dilo.sample.app.R
import kr.co.dilo.sample.app.util.debug
import kr.co.dilo.sdk.AdManager

class SampleApplication : Application() {

    lateinit var adManager: AdManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        debug("SampleApplication.onCreate()")

        PreferenceManager.setDefaultValues(this, R.xml.fragment_settings, false)

//        if (BuildConfig.DEBUG) {
//            StrictMode.setThreadPolicy(
//                ThreadPolicy.Builder()
//                    .detectAll()
//                    .penaltyLog()
//                    .build()
//            )
//            StrictMode.setVmPolicy(
//                VmPolicy.Builder()
//                    .detectAll()
//                    .penaltyLog()
//                    .build()
//            )
//        }
        adManager = AdManager(this)
    }

    companion object {
        lateinit var instance: SampleApplication
    }
}
