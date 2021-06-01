package kr.co.dilo.sample.app;

import android.app.Application;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.util.Log;
import androidx.annotation.NonNull;
import kr.co.dilo.sample.app.util.DiloSampleAppUtil;
import kr.co.dilo.sdk.AdManager;

public class SampleApplication extends Application {

    public AdManager adManager;

    private static SampleApplication instance;

    public static SampleApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DiloSampleAppUtil.LOG_TAG, "SampleApplication.onCreate()");

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        instance = this;
        adManager = new AdManager(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
