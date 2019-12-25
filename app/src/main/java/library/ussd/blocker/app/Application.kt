package library.ussd.blocker.app

import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import library.ussd.blocker.BuildConfig
import android.app.Application as Base

class Application : Base() {

    override fun onCreate() {
        super.onCreate()

        AppCenter.start(this, BuildConfig
            .APP_CENTER_KEY, Analytics::class.java, Crashes::class.java)
    }
}