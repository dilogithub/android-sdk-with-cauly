package kr.co.dilo.sample.app;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;
import androidx.annotation.NonNull;
import kr.co.dilo.sdk.AdManager;
import kr.co.dilo.sdk.DiloUtil;

public class SampleApplication extends Application {

    public AdManager adManager;

    private static SampleApplication instance;

    public static SampleApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DiloUtil.LOG_TAG, "SampleApplication.onCreate()");

        instance = this;
        adManager = new AdManager(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
