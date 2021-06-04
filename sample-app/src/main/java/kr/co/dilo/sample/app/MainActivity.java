package kr.co.dilo.sample.app;

import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import kr.co.dilo.sample.app.content.DummyContent;
import kr.co.dilo.sample.app.databinding.ActivityMainBinding;
import kr.co.dilo.sample.app.fragment.HomeFragment;
import kr.co.dilo.sample.app.fragment.LogFragment;
import kr.co.dilo.sample.app.fragment.SettingsFragment;
import kr.co.dilo.sample.app.util.DiloSampleAppUtil;
import kr.co.dilo.sdk.AdManager;

/**
 * 메인 화면
 */
public class MainActivity extends AppCompatActivity {

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private final HomeFragment homeFragment = new HomeFragment();
    private final LogFragment logFragment = new LogFragment();
    private final SettingsFragment settingFragment = new SettingsFragment();

    /**
     * 광고나 오디오 재생 시 BottomNavigationView 위에 뜨는 재생 화면 창 (검정색 배경)
     */
    private ViewGroup floatingContent;
    private ProgressBar progressBar;
    private TextView contentStat;
    private Intent contentIntent;

    private ActivityMainBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onCreate()");
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(viewBinding.getRoot());

        // contentActionReceiver 가 등록되어있지 않다면 등록
        Intent receiverIntent = new Intent(this, contentActionReceiver.getClass());
        PendingIntent receiverPendingIntent = PendingIntent.getBroadcast(this, 0, receiverIntent, PendingIntent.FLAG_NO_CREATE);
        if (receiverPendingIntent == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END);
            filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY_PAUSE);
            filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY);
            filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_AD);
            filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_LOG);
            filter.addAction(DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS);
            registerReceiver(contentActionReceiver, filter);
        }

        floatingContent = viewBinding.floatingContent;
        progressBar = viewBinding.progressBar;
        contentStat = viewBinding.contentStat;
        contentIntent = new Intent(this, ContentActivity.class);
        floatingContent.setOnClickListener(v -> {
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(contentIntent);
        });

        // 프래그먼트 추가 (홈/로그/설정 화면)
        fragmentManager.beginTransaction()
                .add(R.id.content_list_layout, homeFragment)
                .show(homeFragment)
                .add(R.id.content_list_layout, logFragment)
                .hide(logFragment)
                .add(R.id.content_list_layout, settingFragment)
                .hide(settingFragment)
                .commit();

        // 바텀 네이게이션 뷰 (아래 고정 메뉴)
        BottomNavigationView bottomNavigationView = viewBinding.bottomNavigationView;
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.page_home) {
                fragmentManager.beginTransaction()
                        .hide(logFragment)
                        .hide(settingFragment)
                        .show(homeFragment)
                        .commit();
            } else if (id == R.id.page_log) {
                fragmentManager.beginTransaction()
                        .hide(settingFragment)
                        .hide(homeFragment)
                        .show(logFragment)
                        .commit();
            } else if (id == R.id.page_pref) {
                fragmentManager.beginTransaction()
                        .hide(logFragment)
                        .hide(homeFragment)
                        .show(settingFragment)
                        .commit();
            }
            return true;
        });
    }

    /**
     * 로그 화면으로 로그 전송
     * @param message 로그 내용
     */
    public void log(String message) {
        logFragment.log(message);
    }

    @Override
    protected void onDestroy() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onDestroy()");
        super.onDestroy();

        try {
            unregisterReceiver(contentActionReceiver);
        } catch (IllegalArgumentException ignored) {}

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // 앱 강제 종료 허용 여부에 따라 서비스 및 프로세스 종료
        if (!prefs.getBoolean(DiloSampleAppUtil.PREF_DILO_USE_BACKGROUND, true)) {
            AdManager adManager = SampleApplication.getInstance().adManager;
            adManager.release();

            new Handler(Looper.getMainLooper())
                    .postDelayed(this::exitApplication, 500);
        }
    }

    @Override
    protected void onStart() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onStop()");
        super.onStop();
    }

    /**
     * 뒤로가기 키 눌렀을 때 처리
     */
    @Override
    public void onBackPressed() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onBackPressed()");
        new AlertDialog.Builder(this)
                .setMessage("종료하시겠습니까?")
                .setPositiveButton("종료", (dialog, which) -> finishAffinity())
                .setNegativeButton("취소", (dialog, which) -> dialog.cancel())
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

    /**
     * 컨텐츠 액션 수신 리시버
     */
    BroadcastReceiver contentActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            switch (action) {
                // 컨텐츠 재생 완료
                // 재생 화면 숨김
                case DiloSampleAppUtil.CONTENT_ACTION_PLAY_END:
                    floatingContent.setVisibility(View.GONE);
                    break;

                // 컨텐츠 재생
                case DiloSampleAppUtil.CONTENT_ACTION_PLAY:
                    setFloatingContent(intent);

                    contentStat.setText("재생중");
                    floatingContent.setVisibility(View.VISIBLE);
                    break;

                // 광고 재생
                case DiloSampleAppUtil.CONTENT_ACTION_AD:
                    setFloatingContent(intent);

                    String adInfo = intent.getStringExtra("adInfo");
                    double current = intent.getDoubleExtra("currentSec", 0);
                    double total= intent.getDoubleExtra("totalSec", 0);

                    progressBar.setProgress((int) (current * 100 / total));

                    contentStat.setText(String.format("광고중 %s\n%s / %s",
                            adInfo,
                            DiloSampleAppUtil.secondsToTimeString(current),
                            DiloSampleAppUtil.secondsToTimeString(total)
                    ));
                    floatingContent.setVisibility(View.VISIBLE);
                    break;

                // 컨텐츠 관련 로그 수신
                case DiloSampleAppUtil.CONTENT_ACTION_LOG:
                    String msg = intent.getStringExtra("msg");
                    log(msg);
                    break;

                // 컨텐츠 재생 정보 수신
                case DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS:
                    setFloatingContent(intent);
                    current = intent.getDoubleExtra("currentSec", 0);
                    total = intent.getDoubleExtra("totalSec", 0);
                    progressBar.setProgress((int)(current * 100 / total));
                    contentStat.setText(String.format("재생중\n%s / %s",
                            DiloSampleAppUtil.secondsToTimeString(current),
                            DiloSampleAppUtil.secondsToTimeString(total))
                    );
                    floatingContent.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    // ContentActivity 의 결과 수신해서 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(DiloSampleAppUtil.LOG_TAG, "MainActivity.onActivityResult :: requestCode : " + requestCode + ", resultCode : " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != -1) {
            setFloatingContent(data);
            floatingContent.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 재생 화면 처리
     * @param intent 재생 데이터가 있는 인텐트
     */
    private void setFloatingContent(Intent intent) {
        if (intent == null) {
            return;
        }

        TextView desc = viewBinding.contentSmallDesc;
        DummyContent.DummyItem item = intent.getParcelableExtra("item");
        contentIntent.putExtra("index", intent.getExtras().getInt("index"))
                .putExtra("item", item);
        desc.setText(item.desc);

        TextView title = viewBinding.contentSmallTitle;
        title.setText(item.title);

        ImageView image = viewBinding.contentSmallImage;
        image.setImageResource(item.image);
    }

    /**
     * 어플리케이션 (프로세스) 종료
     */
    private void exitApplication() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "Exiting Application");
        ActivityCompat.finishAffinity(this);
//        System.runFinalizersOnExit(true);
        System.exit(0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(0, R.anim.slide_down_exit);
    }
}
