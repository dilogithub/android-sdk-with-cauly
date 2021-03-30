package kr.co.dilo.sample.app;

import android.content.*;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import kr.co.dilo.sample.app.fragment.*;
import kr.co.dilo.sample.app.content.DummyContent;
import kr.co.dilo.sample.app.util.DiloSampleAppUtil;
import kr.co.dilo.sdk.DiloConst;
import kr.co.dilo.sdk.DiloError;
import kr.co.dilo.sdk.DiloUtil;
import kr.co.dilo.sdk.model.AdInfo;
import kr.co.dilo.sdk.model.Progress;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private final HomeFragment homeFragment = new HomeFragment();
    private final LogFragment logFragment = new LogFragment();
    private final SettingsFragment settingFragment = new SettingsFragment();

    private ViewGroup floatingContent;
    private TextView contentStat;
    private Intent contentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END);
        filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY_PAUSE);
        filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY);
        filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_AD);
        filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_LOG);
        filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS);

        registerReceiver(contentActionReceiver, filter);

        floatingContent = (ViewGroup) findViewById(R.id.floating_content);
        contentStat = (TextView) findViewById(R.id.content_stat);
        contentIntent = new Intent(this, ContentActivity.class);
        floatingContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(contentIntent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        // 첫 화면 지정
        fragmentManager.beginTransaction().add(R.id.frameLayout, homeFragment).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, logFragment).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, settingFragment).commit();
        fragmentManager.beginTransaction().show(homeFragment).commit();
        fragmentManager.beginTransaction().hide(logFragment).commit();
        fragmentManager.beginTransaction().hide(settingFragment).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_home:
                        fragmentManager.beginTransaction().hide(logFragment).commit();
                        fragmentManager.beginTransaction().hide(settingFragment).commit();
                        fragmentManager.beginTransaction().show(homeFragment).commit();
                        break;
                    case R.id.page_log:
                        fragmentManager.beginTransaction().hide(settingFragment).commit();
                        fragmentManager.beginTransaction().hide(homeFragment).commit();
                        fragmentManager.beginTransaction().show(logFragment).commit();
                        break;
                    case R.id.page_pref:
                        fragmentManager.beginTransaction().hide(logFragment).commit();
                        fragmentManager.beginTransaction().hide(homeFragment).commit();
                        fragmentManager.beginTransaction().show(settingFragment).commit();
                        break;
                }
                return true;
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void log(String message) {
        logFragment.log(message);
    }

    @Override
    protected void onDestroy() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onDestroy()");
        unregisterReceiver(contentActionReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onStart()");
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onStop()");
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onBackPressed()");
        new AlertDialog.Builder(this)
                .setMessage("종료하시겠습니까?")
                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onPause()");
        super.onPause();
    }

    BroadcastReceiver contentActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            Log.v(DiloSampleAppUtil.LOG_TAG, "MainActivity.BroadcastReceiver.onReceive() :: Action : " + action);

            switch (action) {
                case DiloSampleAppUtil.CONTENT_ACTION_PLAY_END:
                    floatingContent.setVisibility(View.GONE);
                    break;

                case DiloSampleAppUtil.CONTENT_ACTION_PLAY:
                    setFloatingContent(intent);

                    contentStat.setText("재생중");
                    floatingContent.setVisibility(View.VISIBLE);
                    break;

                case DiloSampleAppUtil.CONTENT_ACTION_AD:
                    setFloatingContent(intent);

                    String adInfo = intent.getStringExtra("adInfo");
                    double current1 = intent.getDoubleExtra("currentSec", 0);
                    double total1 = intent.getDoubleExtra("totalSec", 0);

                    contentStat.setText(String.format("광고중 %s\n%s / %s",
                            adInfo,
                            DiloSampleAppUtil.secondsToTimeString(current1),
                            DiloSampleAppUtil.secondsToTimeString(total1)
                    ));
                    floatingContent.setVisibility(View.VISIBLE);
                    break;

                case DiloSampleAppUtil.CONTENT_ACTION_LOG:
                    String msg = intent.getStringExtra("msg");
                    log(msg);
                    break;

                case DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS:
                    setFloatingContent(intent);
                    double current2 = intent.getDoubleExtra("currentSec", 0);
                    double total2 = intent.getDoubleExtra("totalSec", 0);
                    contentStat.setText(String.format("재생중\n%s / %s",
                            DiloSampleAppUtil.secondsToTimeString(current2),
                            DiloSampleAppUtil.secondsToTimeString(total2))
                    );
                    floatingContent.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onActivityResult :: requestCode : " + requestCode + ", resultCode : " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {
//            floatingContent.setVisibility(View.GONE);
        } else {
            setFloatingContent(data);
            floatingContent.setVisibility(View.VISIBLE);
        }
    }

    private void setFloatingContent(Intent intent) {
        if (intent == null) {
            return;
        }
        TextView desc = (TextView) findViewById(R.id.content_small_desc);
        DummyContent.DummyItem item = (DummyContent.DummyItem) intent.getSerializableExtra("item");
        contentIntent.putExtra("index", intent.getExtras().getInt("index"))
                .putExtra("item", item);
        desc.setText(item.desc);

        TextView title = (TextView) findViewById(R.id.content_small_title);
        title.setText(item.title);

        ImageView image = (ImageView) findViewById(R.id.content_small_image);
        image.setImageResource(item.image);
    }
}
