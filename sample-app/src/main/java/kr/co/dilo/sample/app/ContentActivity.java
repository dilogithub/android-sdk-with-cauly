package kr.co.dilo.sample.app;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import kr.co.dilo.sample.app.content.DummyContent;
import kr.co.dilo.sample.app.databinding.ActivityContentBinding;
import kr.co.dilo.sample.app.fragment.LogFragment;
import kr.co.dilo.sample.app.util.DiloSampleAppUtil;
import kr.co.dilo.sample.app.util.OnSwipeTouchListener;
import kr.co.dilo.sdk.AdManager;
import kr.co.dilo.sdk.AdView;
import kr.co.dilo.sdk.DiloUtil;
import kr.co.dilo.sdk.RequestParam;
import kr.co.dilo.sdk.model.AdInfo;
import kr.co.dilo.sdk.model.DiloError;
import kr.co.dilo.sdk.model.Progress;

/**
 * 광고와 컨텐츠(샘플 영상)을 보여주는 액티비티
 */
public class ContentActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // Content
    private ViewGroup contentWrapper;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private TextView contentTitle;
    private CountDownTimer timer;

    // Ad
    private AdView companionAdView;
    private ViewGroup adWrapper;
    private Button skipButton;
    private Button play;
    private Button pause;
    private Button reload;
    private Button release;
    private AdManager adManager;
    private ViewGroup companionCloseButton;
    private ProgressBar progressBar;
    private EditText progressText;
    private EditText adCount;
    private EditText adTitle;
    private ViewGroup adInfoWrapper;
    private RequestParam.Builder requestParamBuilder = null;
    private Intent contentIntent;
    private DummyContent.DummyItem item;
    private long skipOffset;

    private boolean isPlaying = false;
    private boolean isMediaControllerShowing = false;

    private SharedPreferences pref;

    private ActivityContentBinding viewBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.onCreate()");
        super.onCreate(savedInstanceState);

        viewBinding = ActivityContentBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        viewBinding.getRoot().setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeBottom() {
                super.onSwipeBottom();
                onBackPressed();
            }
        });

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        contentIntent = getIntent();

        adManager = SampleApplication.getInstance().adManager;

        setResult(-1, contentIntent);

        contentWrapper = viewBinding.contentWrapper;
        contentTitle = viewBinding.contentTitle;
        item = (DummyContent.DummyItem) getIntent().getSerializableExtra("item");
        if (item != null) {
            contentTitle.setText(String.format("%s - %s", item.title, item.desc));
        }
        companionAdView = viewBinding.companionAdView;
        adWrapper = viewBinding.adWrapper;
        skipButton = viewBinding.skipButton;
        play = viewBinding.play;
        pause = viewBinding.pause;
        reload = viewBinding.reload;
        release = viewBinding.release;

        surfaceView = viewBinding.videoView;
        companionCloseButton = viewBinding.companionCloseButton;
        progressBar = viewBinding.progressBar;
        progressText = viewBinding.progressText;
        adTitle = viewBinding.adTitle;
        adCount = viewBinding.adCount;
        adInfoWrapper = viewBinding.adInfo;

        play.setOnClickListener(v -> {
            // 로그 초기화
            log(LogFragment.CLEAR_LOG);
            int adRequestDelay = Integer.parseInt(pref.getString("ad_request_delay", "0"));
            if (adRequestDelay > 0) {
                String msg = adRequestDelay + "초 요청 지연";
                log(msg);
                Toast.makeText(ContentActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            new Handler(Looper.getMainLooper())
                    .postDelayed(this::onAdRequest, adRequestDelay * 1000L);
        });
        skipButton.setOnClickListener(v -> {
            if (adManager != null) {
                adManager.skip();
            }
        });
        pause.setOnClickListener(v -> {
            if (adManager != null) {
                adManager.playOrPause();
                sendBroadcast(new Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_PAUSE).addFlags(Intent.FLAG_RECEIVER_FOREGROUND));
            }
        });
        reload.setOnClickListener(v -> {
            if (adManager != null) {
                adWrapper.setVisibility(View.VISIBLE);
//                adInfoWrapper.setVisibility(View.VISIBLE);
                adManager.reloadCompanion(companionAdView, companionCloseButton);
            }
        });
        release.setOnClickListener(v -> {
            if (adManager != null) {
                adManager.release();
                adWrapper.setVisibility(View.INVISIBLE);
                adInfoWrapper.setVisibility(View.INVISIBLE);
                playContent();
            }
        });

        surfaceView.setOnClickListener(v -> {
            if (mediaController != null) {
                if (!isMediaControllerShowing) {
                    mediaController.show();
                    isMediaControllerShowing = true;
                } else {
                    mediaController.hide();
                    isMediaControllerShowing = false;
                }
            }
        });

        contentWrapper.setVisibility(View.INVISIBLE);

        Intent receiverIntent = new Intent(getApplication(), diloActionReceiver.getClass());
        PendingIntent receiverPendingIntent = PendingIntent.getBroadcast(this, 0, receiverIntent, PendingIntent.FLAG_NO_CREATE);
        if (receiverPendingIntent == null) {
            getApplication().registerReceiver(diloActionReceiver, DiloUtil.DILO_INTENT_FILTER);
        }

        adWrapper.setVisibility(View.INVISIBLE);
        adInfoWrapper.setVisibility(View.INVISIBLE);
        playContent();
    }

    @Override
    public void onDestroy() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.onDestroy()");
        try {
            getApplication().unregisterReceiver(diloActionReceiver);
        } catch (IllegalArgumentException ignored) {}
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.surfaceCreated()");

        if (mediaPlayer != null) {
            try {
                mediaPlayer.setDisplay(holder);
            } catch (IllegalStateException e) {
                return;
            }
            mediaController = new MediaController(this);

            mediaPlayer.setOnPreparedListener(mp -> {

                int duration = mediaPlayer.getDuration();
                if (timer != null) {
                    timer.cancel();
                }
                timer = new CountDownTimer(duration, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        double currentSec = (double) mediaPlayer.getCurrentPosition() / 1000;
                        double totalSec = (double) duration / 1000;

                        Intent i = new Intent(DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS)
                                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                                .putExtra("item", item)
                                .putExtra("currentSec", currentSec)
                                .putExtra("totalSec", totalSec);
                        sendBroadcast(i);
                    }

                    @Override
                    public void onFinish() {

                    }
                };

                log("컨텐츠 재생");
                sendBroadcast(getContentPlayIntent());
                isPlaying = true;
                if (timer != null) {
                    timer.start();
                }
                mp.start();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
//                    log("컨텐츠 재생 실패");
                return false;
            });

            mediaController.setMediaPlayer(new MediaController.MediaPlayerControl() {
                @Override
                public void start() {
                    log("컨텐츠 재생");
                    if (timer != null) {
                        timer.start();
                    }
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                }

                @Override
                public void pause() {
                    if (timer != null) {
                        timer.cancel();
                    }
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                    }
                }

                @Override
                public int getDuration() {
                    if (mediaPlayer != null) {
                        return mediaPlayer.getDuration();
                    }
                    return 0;
                }

                @Override
                public int getCurrentPosition() {
                    if (mediaPlayer != null) {
                        return mediaPlayer.getCurrentPosition();
                    }

                    return 0;
                }

                @Override
                public void seekTo(int pos) {
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(pos);
                    }
                }

                @Override
                public boolean isPlaying() {
                    if (mediaPlayer != null) {
                        return mediaPlayer.isPlaying();
                    }
                    return false;
                }

                @Override
                public int getBufferPercentage() {
                    return 0;
                }

                @Override
                public boolean canPause() {
                    return true;
                }

                @Override
                public boolean canSeekBackward() {
                    return true;
                }

                @Override
                public boolean canSeekForward() {
                    return true;
                }

                @Override
                public int getAudioSessionId() {
                    if (mediaPlayer != null) {
                        return mediaPlayer.getAudioSessionId();
                    }
                    return Integer.MIN_VALUE;
                }
            });
            mediaController.setAnchorView(surfaceView);

            if (isPlaying) {
                return;
            }

            String dataSource = "android.resource://" + this.getPackageName() + "/raw/sample";
            Log.v(DiloSampleAppUtil.LOG_TAG, "ContentActivity.surfaceCreated() :: 컨텐츠 소스 설정 Url : " + dataSource);
            try {
                mediaPlayer.setDataSource(this, Uri.parse(dataSource));
            } catch (Exception e) {
                Log.e(DiloSampleAppUtil.LOG_TAG, "ContentActivity.surfaceCreated() :: 컨텐츠 소스를 설정할 수 없습니다");
            }

            try {
                mediaPlayer.prepareAsync();
            } catch (IllegalStateException ignored) {}
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {}

    /**
     * 광고 요청
     */
    private void onAdRequest() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.onAdRequest()");

        if (timer != null) {
            timer.cancel();
        }

        isPlaying = false;

        Intent i = new Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        sendBroadcast(i);
        setResult(-1, contentIntent);

        PendingIntent notificationIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // SharedPreferences 에서 값 읽어 옴(테스트 용)
        final String epiCode = pref.getString("epi_code", "").trim();
        final String bundleId = pref.getString("package_name", "").trim();
        final String channelName = pref.getString("channel_name", "").trim();
        final String episodeName = pref.getString("episode_name", "").trim();
        final String creatorId = pref.getString("creator_id", "").trim();
        final String creatorName = pref.getString("creator_name", "").trim();
        final int duration = Integer.parseInt(pref.getString("duration", "15"));
        final RequestParam.ProductType productType = RequestParam.ProductType.valueOf(pref.getString("product_type", "DILO_PLUS"));
        final RequestParam.FillType fillType = RequestParam.FillType.valueOf(pref.getString("fill_type", "MULTI"));
        final RequestParam.AdPositionType adPositionType = RequestParam.AdPositionType.valueOf(pref.getString("ad_position_type", "PRE"));
        final boolean usePauseInNotification = pref.getBoolean("use_pause_in_notification", true);
        final boolean useProgressBarInNotification = pref.getBoolean("use_progress_bar_in_notification", true);

        requestParamBuilder =
                new RequestParam.Builder(this)
                        // 필수 항목
                        .bundleId(bundleId)                              // 패키지 설정
                        .epiCode(epiCode)                                // 에피소드 코드 설정
                        .productType(productType)                        // 광고 상품 유형 설정
                        .fillType(fillType)                              // 광고 채우기 유형 설정
                        .drs(duration)                                   // 광고 시간 설정
                        .adPositionType(adPositionType)                  // 광고 재생 시점 설정
                        .iconResourceId(R.drawable.notification_icon)    // Notification 아이콘 설정
                        .channelName(channelName)                        // 채널 이름 설정
                        .episodeName(episodeName)                        // 에피소드 이름 설정
                        .creatorId(creatorId)                            // 크리에이터 ID (식별자) 설정
                        .creatorName(creatorName)                        // 크리에이터 이름 설정
                        // 선택 항목
                        .companionAdView(companionAdView)                // Companion View 설정 (Companion 이 있는 광고가 나가려면 필수)
                        .closeButton(companionCloseButton)               // 닫기 버튼 설정
                        .skipButton(skipButton)                          // Skip 버튼 설정
                        .notificationContentIntent(notificationIntent)   // Notification Click PendingIntent 설정
                        .notificationContentTitle(pref.getString("notification_title", "")) // Notification Title 설정 (상단 텍스트)
                        .notificationContentText(pref.getString("notification_text", ""));  // Notification Text 설정 (하단 텍스트)

        if (!pref.getBoolean("companion_size", false)) {
            requestParamBuilder.companionSize(
                    Integer.parseInt(pref.getString("companion_width", "300")),
                    Integer.parseInt(pref.getString("companion_height", "300"))
            );
        }

        // 플래그 설정
        if (usePauseInNotification) {
            requestParamBuilder.addFlags(RequestParam.FLAG_USE_PAUSE_IN_NOTIFICATION);
        }
        if (useProgressBarInNotification) {
            requestParamBuilder.addFlags(RequestParam.FLAG_USE_PROGRESSBAR_IN_NOTIFICATION);
        }

        log("광고 요청");
        log("========================================");
        log("광고 요청 정보");
        log(String.format("패키지 명 : %s", bundleId));
        log(String.format("에피소드  : %s", epiCode));
        log(String.format("광고 상품 : %s", productType));
        log(String.format("광고 갯수 : %s", fillType));
        log(String.format("광고 길이 : %s", duration));
        log("========================================");

        adManager.loadAd(requestParamBuilder.build());
    }

    private void playContent() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.playContent()");
        sendBroadcast(getContentPlayIntent());

        setResult(contentIntent.getExtras().getInt("index", -1), contentIntent);

        contentWrapper.setVisibility(View.VISIBLE);
        if (mediaController != null) {
            mediaController.setVisibility(View.VISIBLE);
        }

        if (mediaPlayer != null) {
            log("컨텐츠 이어 재생");
            if (timer != null) {
                timer.start();
            }
            mediaPlayer.start();
            isPlaying = true;
            return;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setOnCompletionListener(mp -> {
            Intent i = new Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END)
                    .addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            sendBroadcast(i);
            setResult(-1, contentIntent);
            if (timer != null) {
                timer.cancel();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            mediaController = null;
        });
        mediaPlayer.setLooping(false);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(this);
        // surfaceCreated 강제 호출
        surfaceView.setVisibility(View.GONE);
        surfaceView.setVisibility(View.VISIBLE);
    }

    private Intent getContentPlayIntent() {
        return new Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                .putExtra("index", contentIntent.getExtras().getInt("index"))
                .putExtra("item", item);
    }

    public void log(String message) {
        Intent i = new Intent(DiloSampleAppUtil.CONTENT_ACTION_LOG)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                .putExtra("msg", message);
        sendBroadcast(i);
    }

    @Override
    public void onBackPressed() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.onBackPressed()");

        try {
            if (isPlaying || (mediaPlayer != null && mediaPlayer.isPlaying()) || (adManager != null && adManager.isPlaying())) {
                Intent intent = new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else {
                super.onBackPressed();
            }
        } catch (IllegalStateException e) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.onStop()");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(DiloSampleAppUtil.LOG_TAG, "ContentActivity.onResume()");
        super.onResume();
    }

    /**
     * 딜로 SDK 로부터 액션을 받는 BroadcastReceiver
     */
    @SuppressLint("DefaultLocale")
    BroadcastReceiver diloActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        // 컴패니언 리로드 액션
                        case DiloUtil.ACTION_RELOAD_COMPANION:
                            if (adManager != null) {
                                adWrapper.setVisibility(View.VISIBLE);
                                adManager.reloadCompanion(companionAdView, companionCloseButton);
                            }
                            break;

                        // 사용자의 컴패니언 닫기 액션
                        case DiloUtil.ACTION_ON_COMPANION_CLOSED:
                            log("사용자가 컴패니언을 닫았습니다");
                            break;

                        // 광고 준비 액션
                        case DiloUtil.ACTION_ON_AD_READY:
                            log("광고 준비 완료");

                            ContentActivity.this.setResult(Integer.MIN_VALUE, contentIntent);

                            if (mediaPlayer != null) {
                                log("컨텐츠 일시 중지");
                                try {
                                    mediaPlayer.pause();
                                } catch (IllegalStateException ignored) {}
                            }

                            contentWrapper.setVisibility(View.INVISIBLE);
                            if (mediaController != null) {
                                mediaController.setVisibility(View.INVISIBLE);
                            }

                            adManager.start();
                            isPlaying = true;
                            ContentActivity.this.setResult(contentIntent.getExtras().getInt("index", -1), contentIntent);
                            adWrapper.setVisibility(View.VISIBLE);
                            log("광고 재생");
                            break;

                        // 광고 플레이 시작 액션
                        case DiloUtil.ACTION_ON_AD_START:
                            skipOffset = 0;
                            AdInfo adInfo = (AdInfo) intent.getSerializableExtra(DiloUtil.EXTRA_AD_INFO);
                            Intent i = new Intent(DiloSampleAppUtil.CONTENT_ACTION_AD)
                                    .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                                    .putExtra("index", intent.getExtras().getInt("index"))
                                    .putExtra("item", item)
                                    .putExtra("currentSec", 0D)
                                    .putExtra("totalSec", 0D)
                                    .putExtra("adInfo", String.format("%d/%d", adInfo.currentOffset, adInfo.totalCount));
                            sendBroadcast(i);

                            adCount.setText(String.format("광고 %d/%d", adInfo.currentOffset, adInfo.totalCount));
                            adTitle.setText(String.format("[%s] %s", adInfo.advertiserName, adInfo.title));
                            log("========================================");
                            log("광고 정보");
                            log(String.format("타입     : %s", adInfo.type));
                            log(String.format("광고주   : %s", adInfo.advertiserName));
                            log(String.format("광고명   : %s", adInfo.title));
                            log(String.format("길이     : %d초", adInfo.duration));
                            log(String.format("광고 수  : %d/%d", adInfo.currentOffset, adInfo.totalCount));
                            log(String.format("컴패니언 : %s", adInfo.hasCompanion ? "있음" : "없음"));
                            log(String.format("스킵 %s", adInfo.skipOffset != 0 ? "가능 " + DiloSampleAppUtil.secondsToTimeString(adInfo.skipOffset) : "불가능"));
                            log("========================================");
                            log("재생이 시작되었습니다");

                            skipOffset = adInfo.skipOffset;

                            if (adInfo.skipOffset == 0) {
                                skipButton.setText("스킵 불가능");
                                skipButton.setVisibility(View.VISIBLE);
                            }

                            adWrapper.setVisibility(View.VISIBLE);
                            adInfoWrapper.setVisibility(View.VISIBLE);
                            break;

                        // 광고 재생 완료 액션 (각각의 광고 재생 완료마다 호출)
                        case DiloUtil.ACTION_ON_AD_COMPLETED:
                            log("재생이 완료되었습니다");
                            break;

                        // 모든 광고 재생 완료 액션 (가장 마지막 광고 재생 완료 시 한 번 호출)
                        case DiloUtil.ACTION_ON_ALL_AD_COMPLETED:
                            log("모든 광고 재생이 완료되었습니다");
                            break;

                        // 광고 일시 중지 액션
                        case DiloUtil.ACTION_ON_PAUSE:
                            log("일시중지");
                            break;

                        // 광고 재개 액션
                        case DiloUtil.ACTION_ON_RESUME:
                            log("재개");
                            break;

                        // 요청한 조건에 맞는 광고 없음 액션
                        case DiloUtil.ACTION_ON_NO_FILL:
                            log("광고가 없습니다 (No Fill)");
                            Toast.makeText(ContentActivity.this, "광고가 없습니다 (No Fill)", Toast.LENGTH_SHORT).show();
                            adWrapper.setVisibility(View.INVISIBLE);
                            adInfoWrapper.setVisibility(View.INVISIBLE);
                            playContent();
                            break;

                        // 스킵 가능 시점 도달 액션
                        case DiloUtil.ACTION_ON_SKIP_ENABLED:
                            log("스킵 가능 시점 도달");
                            if (companionAdView.getVisibility() == View.VISIBLE) {
                                skipButton.setVisibility(View.VISIBLE);
                            }
                            break;

                        // 에러 발생 액션
                        case DiloUtil.ACTION_ON_ERROR:
                            DiloError error = (DiloError) intent.getSerializableExtra(DiloUtil.EXTRA_ERROR);
                            log(String.format("광고 요청 중 에러가 발생하였습니다\n\t타입: %s, 에러: %s, 상세: %s", error.type, error.error, error.detail));
                            playContent();
                            break;

                        // 광고 진행 사항 업데이트 액션
                        case DiloUtil.ACTION_ON_TIME_UPDATE:
                            Progress progress = (Progress) intent.getSerializableExtra(DiloUtil.EXTRA_PROGRESS);

                            int percent = (int) (progress.seconds * 100 / progress.duration);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                progressBar.setProgress(percent, false);
                            } else {
                                progressBar.setProgress(percent);
                            }
                            log(String.format(
                                    "광고 진행정보 :: %5dms / %5dms [%3d%%]",
                                    (int) (progress.seconds * 1000),
                                    (int) (progress.duration * 1000),
                                    percent)
                            );

                            progressText.setText(
                                    String.format("%s / %s",
                                            DiloSampleAppUtil.secondsToTimeString(progress.seconds),
                                            DiloSampleAppUtil.secondsToTimeString(progress.duration)
                                    ),
                                    TextView.BufferType.NORMAL
                            );
                            adCount.setText(String.format("광고 %d/%d", progress.current, progress.total));

                            if (skipOffset != 0) {
                                String msg = "건너뛰기";
                                if (skipOffset - (int) progress.seconds > 0) {
                                    msg = (int) (skipOffset - progress.seconds) + "초 후 건너뛰기";
                                    skipButton.setVisibility(View.VISIBLE);
                                }
                                skipButton.setText(msg);
                            }

                            i = new Intent(DiloSampleAppUtil.CONTENT_ACTION_AD)
                                    .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                                    .putExtra("index", intent.getExtras().getInt("index"))
                                    .putExtra("item", item)
                                    .putExtra("currentSec", progress.seconds)
                                    .putExtra("totalSec", progress.duration)
                                    .putExtra("adInfo", String.format("%d/%d", progress.current, progress.total));
                            sendBroadcast(i);
                            break;

                        // 사용자 광고 스킵 액션
                        case DiloUtil.ACTION_ON_AD_SKIPPED:
                            log("사용자가 광고를 건너뛰었습니다");
                            break;

                        // SDK 로부터 메시지 수신
                        case DiloUtil.ACTION_ON_MESSAGE:
                            String msg = intent.getStringExtra(DiloUtil.EXTRA_MESSAGE);
                            log(msg);
                            break;

                        case DiloUtil.ACTION_ON_SVC_DESTROYED:
                            log("딜로 SDK 서비스 종료");
//                            adWrapper.setVisibility(View.INVISIBLE);
                            adInfoWrapper.setVisibility(View.INVISIBLE);
                            playContent();
                            break;
                    }
                }
            }
        }
    };

}
