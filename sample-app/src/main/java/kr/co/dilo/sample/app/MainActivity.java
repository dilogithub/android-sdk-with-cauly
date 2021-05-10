package kr.co.dilo.sample.app;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import kr.co.dilo.sample.app.content.DummyContent;
import kr.co.dilo.sample.app.fragment.HomeFragment;
import kr.co.dilo.sample.app.fragment.LogFragment;
import kr.co.dilo.sample.app.fragment.SettingsFragment;
import kr.co.dilo.sample.app.util.DiloSampleAppUtil;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

        Intent receiverIntent = new Intent(this, contentActionReceiver.getClass());
        PendingIntent receiverPendingIntent = PendingIntent.getBroadcast(this, 0, receiverIntent, PendingIntent.FLAG_NO_CREATE);
        // contentActionReceiver가 등록되어있지 않다면 등록
        if (receiverPendingIntent == null) {
            registerReceiver(contentActionReceiver, filter);
        }

        floatingContent = (ViewGroup) findViewById(R.id.floating_content);
        contentStat = (TextView) findViewById(R.id.content_stat);
        contentIntent = new Intent(this, ContentActivity.class);
        floatingContent.setOnClickListener(v -> {
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(contentIntent);
        });

        /**
         * 바텀 네이게이션 뷰 (아래 고정 메뉴)
         */
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        // 프래그먼트 추가 (홈/로그/설정 화면)
        fragmentManager.beginTransaction().add(R.id.frameLayout, homeFragment).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, logFragment).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, settingFragment).commit();
        fragmentManager.beginTransaction().show(homeFragment).commit();
        fragmentManager.beginTransaction().hide(logFragment).commit();
        fragmentManager.beginTransaction().hide(settingFragment).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
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
        });
    }

    /**
     * 로그 화면으로 로그 전송
     * @param message 로그 내용
     */
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
            Log.v(DiloSampleAppUtil.LOG_TAG, "MainActivity.BroadcastReceiver.onReceive() :: Action : " + action);

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
                    double current1 = intent.getDoubleExtra("currentSec", 0);
                    double total1 = intent.getDoubleExtra("totalSec", 0);

                    contentStat.setText(String.format("광고중 %s\n%s / %s",
                            adInfo,
                            DiloSampleAppUtil.secondsToTimeString(current1),
                            DiloSampleAppUtil.secondsToTimeString(total1)
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

    // ContentActivity의 결과 수신해서 처리
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

    /**
     * 재생 화면 처리
     * @param intent 재생 데이터가 있는 인텐트
     */
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
