package kr.co.dilo.sample.app.ui

import android.app.PendingIntent
import android.content.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.*
import android.widget.*
import android.widget.MediaController.MediaPlayerControl
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fsn.cauly.*
import kr.co.dilo.sample.app.R
import kr.co.dilo.sample.app.databinding.ActivityContentBinding
import kr.co.dilo.sample.app.ui.content.DummyContent
import kr.co.dilo.sample.app.util.DiloSampleAppUtil
import kr.co.dilo.sample.app.util.debug
import kr.co.dilo.sample.app.util.safeParseInt
import kr.co.dilo.sample.app.util.toTimeString
import kr.co.dilo.sdk.*
import kr.co.dilo.sdk.model.AdInfo
import kr.co.dilo.sdk.model.DiloError
import kr.co.dilo.sdk.model.Progress

/**
 * 광고와 컨텐츠(샘플 영상)을 보여주는 액티비티
 */
class ContentActivity : AppCompatActivity(), SurfaceHolder.Callback, CaulyInterstitialAdListener, CaulyAdViewListener {

    // Content
    private lateinit var contentWrapper: ViewGroup
    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private var mediaPlayer: MediaPlayer? = null
    private var mediaController: MediaController? = null
    private lateinit var timer: CountDownTimer

    // Ad
    private lateinit var companionAdView: AdView
    private lateinit var adWrapper: ViewGroup
    private lateinit var skipButton: Button
    private lateinit var adManager: AdManager

    // Ad 제어 View
    private lateinit var play: Button
    private lateinit var pause: Button
    private lateinit var reload: Button
    private lateinit var release: Button
    private lateinit var settings: Button
    private lateinit var companionCloseButton: ViewGroup

    // Ad 정보 View
    private lateinit var progressBar: ProgressBar
    private lateinit var currentTime: TextView
    private lateinit var totalTime: TextView
    private lateinit var adTitle: TextView
    private lateinit var adCount: TextView
    private lateinit var playInfoWrapper: ViewGroup

    private lateinit var contentIntent: Intent
    private var item: DummyContent.DummyItem? = null
    private var skipOffset: Long = 0L
    private var isPlaying: Boolean = false
    private var isMediaControllerShowing: Boolean = false
    private lateinit var prefs: SharedPreferences
    private lateinit var viewBinding: ActivityContentBinding
    private lateinit var log: EditText

    // 카울리 전면 광고
    private var caulyInterstitialAd: CaulyInterstitialAd? = null
    private var showCaulyInterstitial: Boolean = false

    // 카울리 배너 광고
    private var caulyBannerAd: CaulyAdView? = null

    // Automotive 지원을 위한 객체
    private var mediaBrowser: MediaBrowserCompat? = null
    // Automotive 지원을 위한 Callback
    private val mediaBrowserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            debug("ContentActivity.MediaBrowserCompat.ConnectionCallback :: onConnected")
            if (mediaBrowser == null) {
                debug("ContentActivity.MediaBrowserCompat.ConnectionCallback :: onConnected / mediaBrowser is null")
                return
            }

            mediaBrowser?.let {
                if (!it.isConnected) {
                    return
                }
                if (it.sessionToken == null) {
                    debug("ContentActivity.MediaBrowserCompat.ConnectionCallback.onConnected() :: Session Token is null")
                    return
                }

                val mediaBrowserController = MediaControllerCompat(this@ContentActivity, it.sessionToken)

                debug("ContentActivity.MediaBrowserCompat.ConnectionCallback.onConnected() :: ${it.sessionToken}")
                mediaBrowserController.registerCallback(object: MediaControllerCompat.Callback() {
                    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                        debug("""
                        ContentActivity.MediaBrowserCompat.ConnectionCallback.onMetadataChanged() :: 
                        ARTIST : ${metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)} 
                        TITLE : ${metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)}
                        ALBUM URI : ${metadata?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)}
                        """.trimIndent())
                    }
                })
                MediaControllerCompat.setMediaController(this@ContentActivity, mediaBrowserController)
            }
        }

        override fun onConnectionSuspended() {
            debug("ContentActivity.MediaBrowserCompat.ConnectionCallback :: onConnectionSuspended")
        }

        override fun onConnectionFailed() {
            debug("ContentActivity.MediaBrowserCompat.ConnectionCallback :: onConnectionFailed")
        }
    }

    companion object {
        /**
         * 로그 초기화 메시지
         */
        const val CLEAR_LOG = "jmi;oaFwe1cft($*#&E1FLS3D2OIV=+'vm,.x!@8907"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        debug("ContentActivity.onCreate()")
        super.onCreate(savedInstanceState)
        viewBinding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        contentIntent = intent
        adManager = AdManager(this)
        contentWrapper = viewBinding.contentWrapper

        log = viewBinding.logArea
        log.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("로그를 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    log.editableText?.clear()
                }
                .setNegativeButton("취소", null)
                .show()
            false
        }

        companionAdView = viewBinding.companionAdView
        adWrapper = viewBinding.adWrapper
        skipButton = viewBinding.skipButton
        play = viewBinding.play
        pause = viewBinding.pause
        reload = viewBinding.reload
        release = viewBinding.release
        settings = viewBinding.settings
        surfaceView = viewBinding.videoView
        companionCloseButton = viewBinding.companionCloseButton
        progressBar = viewBinding.progressBar
        currentTime = viewBinding.currentTime
        adTitle = viewBinding.adTitle
        totalTime = viewBinding.totalTime
        adCount = viewBinding.adCount
        playInfoWrapper = viewBinding.playInfo

        // Dilo 광고 없거나 오류시 카울리 전면 광고 Fallback
        caulyInterstitialAd = CaulyInterstitialAd().apply {
            setAdInfo(CaulyAdInfoBuilder(getString(R.string.cauly_interstitial_app_code))
                .build()
            )
            setInterstialAdListener(this@ContentActivity)
        }

        // 카울리 배너 광고 닫기 버튼 클릭 이벤트
        viewBinding.caulyBannerClose.setOnClickListener {
            // 배너 광고 닫기 시 완전한 리소스 반환을 위해 아래와 같이 처리 필요
            //   1. CaulyAdView destory() 호출 및 null 처리
            //   2. 카울리 배너 Root View의 removeAllViews() 호출
            caulyBannerAd?.destroy()
            caulyBannerAd = null
            viewBinding.caulyBanner.removeAllViews()
            viewBinding.caulyBannerWrapper.visibility = View.GONE
        }

        play.setOnClickListener {
            // 로그 초기화
            log(CLEAR_LOG)
            val adRequestDelay: Int =
                prefs.getString(DiloSampleAppUtil.PREF_DILO_AD_REQUEST_DELAY, "0").safeParseInt(-1)
            if (adRequestDelay > 0) {
                val msg = "$adRequestDelay 초 요청 지연"
                log(msg)
                Toast.makeText(this@ContentActivity, msg, Toast.LENGTH_SHORT).show()
            }
            Handler(Looper.getMainLooper())
                .postDelayed({ onAdRequest() }, adRequestDelay * 1000L)
        }

        skipButton.setOnClickListener {
            adManager.skip()
        }

        pause.setOnClickListener {
            adManager.run {
                playOrPause()
                sendBroadcast(
                    Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_PAUSE)
                        .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                )
            }
        }

        reload.setOnClickListener {
            if (adManager.reloadCompanion(companionAdView, companionCloseButton)) {
                log("컴패니언 리로드")
                adWrapper.visibility = View.VISIBLE
            }
        }

        release.setOnClickListener {
            adManager.release()
            adWrapper.visibility = View.INVISIBLE
            playContent()
        }

        settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        surfaceView.setOnClickListener {
            mediaController?.run {
                isMediaControllerShowing = if (!isMediaControllerShowing) {
                    show()
                    true
                } else {
                    hide()
                    false
                }
            }
        }

        contentWrapper.visibility = View.INVISIBLE
        val receiverIntent = Intent(application, diloActionReceiver.javaClass)
        val receiverPendingIntent: PendingIntent? =
            PendingIntent.getBroadcast(application, 0, receiverIntent, DiloUtil.setPendingIntentFlagsWithImmutableFlag(PendingIntent.FLAG_NO_CREATE))
        if (receiverPendingIntent == null) {
            application.registerReceiver(diloActionReceiver, DiloUtil.DILO_INTENT_FILTER)
        }

        adWrapper.visibility = View.INVISIBLE
        playContent()
    }

    public override fun onDestroy() {
        debug("ContentActivity.onDestroy()")
        try {
            application.unregisterReceiver(diloActionReceiver)
        } catch (ignored: IllegalArgumentException) {
        }
        super.onDestroy()
    }

    /**
     * 샘플 영상 재생 부분 (매체사에서는 이 메소드 코드 참고하지 않으셔도 됩니다)
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        debug("ContentActivity.surfaceCreated()")

        mediaPlayer?.run mediaPlayer@ {
            try {
                setDisplay(holder)
            } catch (e: IllegalStateException) {
                return
            }
            mediaController = MediaController(this@ContentActivity)
            setOnPreparedListener {
                val duration: Int = duration
                timer = object : CountDownTimer(duration.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val currentSec: Double = (mediaPlayer?.currentPosition?.toDouble() ?: 0.0) / 1000
                        val totalSec: Double = duration.toDouble() / 1000
                        val i: Intent = Intent(DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS)
                            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                            .putExtra("item", item)
                            .putExtra("currentSec", currentSec)
                            .putExtra("totalSec", totalSec)
                        sendBroadcast(i)

                        currentTime.setText(currentSec.toTimeString(), TextView.BufferType.NORMAL)
                        totalTime.setText(totalSec.toTimeString(), TextView.BufferType.NORMAL)

                        item = intent.getParcelableExtra("item")
                        adTitle.text = ("${item?.title} - ${item?.desc}")

                        val percent: Int = (currentSec * 100 / totalSec).toInt()
                        progressBar.progress = percent

                    }

                    override fun onFinish() {}
                }
                log("컨텐츠 재생")
                sendBroadcast(contentPlayIntent)
                this@ContentActivity.isPlaying = true
                timer.start()
                start()
            }

            setOnErrorListener { _: MediaPlayer?, _: Int, _: Int -> false }

            mediaController?.setMediaPlayer(object : MediaPlayerControl {
                override fun start() {
                    log("컨텐츠 재생")
                    timer.start()
                    this@mediaPlayer.start()
                }

                override fun pause() {
                    timer.cancel()
                    this@mediaPlayer.pause()
                }

                override fun getDuration(): Int {
                    return this@mediaPlayer.duration
                }

                override fun getCurrentPosition(): Int {
                    return this@mediaPlayer.currentPosition
                }

                override fun seekTo(pos: Int) {
                    this@mediaPlayer.seekTo(pos)
                }

                override fun isPlaying(): Boolean {
                    return this@mediaPlayer.isPlaying
                }

                override fun getBufferPercentage(): Int {
                    return 0
                }

                override fun canPause(): Boolean {
                    return true
                }

                override fun canSeekBackward(): Boolean {
                    return true
                }

                override fun canSeekForward(): Boolean {
                    return true
                }

                override fun getAudioSessionId(): Int {
                    return this@mediaPlayer.audioSessionId
                }
            })
            mediaController?.setAnchorView(surfaceView)
            if (isPlaying) {
                return
            }
            val dataSource = "android.resource://${this@ContentActivity.packageName}/raw/sample"
            debug("ContentActivity.surfaceCreated() :: 컨텐츠 소스 설정 Url : $dataSource")
            try {
                setDataSource(this@ContentActivity, Uri.parse(dataSource))
            } catch (e: Exception) {
                error("ContentActivity.surfaceCreated() :: 컨텐츠 소스를 설정할 수 없습니다")
            }
            try {
                prepareAsync()
            } catch (ignored: IllegalStateException) {}
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    /**
     * 광고 요청
     */
    private fun onAdRequest() {
        debug("ContentActivity.onAdRequest()")

        val notificationIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(application, MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            DiloUtil.setPendingIntentFlagsWithImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        // SharedPreferences 에서 값 읽어 옴(테스트 용)
        val epiCode:     String = prefs.getString(DiloSampleAppUtil.PREF_DILO_EPI_CODE, "")!!.trim()
        val bundleId:    String = prefs.getString(DiloSampleAppUtil.PREF_DILO_PACKAGE_NAME, "")!!.trim()
        val channelName: String = prefs.getString(DiloSampleAppUtil.PREF_DILO_CHANNEL_NAME, "")!!.trim()
        val episodeName: String = prefs.getString(DiloSampleAppUtil.PREF_DILO_EPISODE_NAME, "")!!.trim()
        val creatorId:   String = prefs.getString(DiloSampleAppUtil.PREF_DILO_CREATOR_ID, "")!!.trim()
        val creatorName: String = prefs.getString(DiloSampleAppUtil.PREF_DILO_CREATOR_NAME, "")!!.trim()
        val duration:       Int = prefs.getString(DiloSampleAppUtil.PREF_DILO_DURATION, "15").safeParseInt(-1)
        val albumArtUri: String = prefs.getString(DiloSampleAppUtil.PREF_DILO_ALBUM_ART_URI, "")!!.trim()

        val productType: RequestParam.ProductType = RequestParam.ProductType.valueOf(
            prefs.getString(DiloSampleAppUtil.PREF_DILO_PRODUCT_TYPE, RequestParam.ProductType.DILO_PLUS.value.uppercase())!!
        )
        val fillType: RequestParam.FillType = RequestParam.FillType.valueOf(
            prefs.getString(DiloSampleAppUtil.PREF_DILO_FILL_TYPE, RequestParam.FillType.MULTI.value.uppercase())!!
        )
        val adPositionType: RequestParam.AdPositionType = RequestParam.AdPositionType.valueOf(
            prefs.getString(DiloSampleAppUtil.PREF_DILO_AD_POSITION_TYPE, RequestParam.AdPositionType.PRE.value.uppercase())!!
        )

        val usePauseInNotification:       Boolean = prefs.getBoolean(DiloSampleAppUtil.PREF_DILO_USE_PAUSE_IN_NOTIFICATION, true)
        val useProgressBarInNotification: Boolean = prefs.getBoolean(DiloSampleAppUtil.PREF_DILO_USE_PROGRESS_BAR_IN_NOTIFICATION, true)

        val requestParamBuilder: RequestParam.Builder = RequestParam.Builder(this).apply {
            // 필수 항목
            bundleId(bundleId)                           // 패키지 설정
            epiCode(epiCode)                             // 에피소드 코드 설정
            productType(productType)                     // 광고 상품 유형 설정
            fillType(fillType)                           // 광고 채우기 유형 설정
            drs(duration)                                // 광고 시간 설정
            adPositionType(adPositionType)               // 광고 재생 시점 설정
            iconResourceId(R.drawable.notification_icon) // Notification 아이콘 설정
            channelName(channelName)                     // 채널 이름 설정
            episodeName(episodeName)                     // 에피소드 이름 설정
            creatorId(creatorId)                         // 크리에이터 ID (식별자) 설정
            creatorName(creatorName)                     // 크리에이터 이름 설정

            // 선택 항목
            companionAdView(companionAdView)            // Companion View 설정 (Companion 이 있는 광고가 나가려면 필수)
            closeButton(companionCloseButton)             // 닫기 버튼 설정
            skipButton(skipButton)                        // Skip 버튼 설정
            albumArtUri(albumArtUri) // 앨범 URI 설정 (Automotive)
            notificationContentIntent(notificationIntent) // Notification Click PendingIntent 설정
            notificationContentTitle(                     // Notification Title 설정 (상단 텍스트)
                prefs.getString(DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TITLE,"")
            )
            notificationContentText(                      // Notification Text 설정 (하단 텍스트)
                prefs.getString(DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TEXT,"")
            )

            // 컴패니언 사이즈 수동 설정
            if (prefs.getBoolean(DiloSampleAppUtil.PREF_DILO_COMPANION_SIZE, false)) {
                companionSize(
                    prefs.getString(DiloSampleAppUtil.PREF_DILO_COMPANION_WIDTH, "300").safeParseInt(300),
                    prefs.getString(DiloSampleAppUtil.PREF_DILO_COMPANION_HEIGHT, "300").safeParseInt(300)
                )
            }

            // 플래그 설정
            if (usePauseInNotification) {
                addFlags(RequestParam.FLAG_USE_PAUSE_IN_NOTIFICATION)
            }
            if (useProgressBarInNotification) {
                addFlags(RequestParam.FLAG_USE_PROGRESSBAR_IN_NOTIFICATION)
            }
        }

        log("광고 요청")
        log("========================================")
        log("광고 요청 정보")
        log("패키지     이름 : $bundleId")
        log("에피소드   코드 : $epiCode")
        log("광고       상품 : $productType")
        log("광고       갯수 : $fillType")
        log("광고       길이 : $duration")
        log("광고  재생 시점 : $adPositionType")
        log("채널       이름 : $channelName")
        log("에피소드   이름 : $episodeName")
        log("크리에이터 이름 : $creatorName")
        log("크리에이터 ID   : $creatorId")
        log("========================================")

        mediaBrowser?.let {
            if (it.isConnected) {
                it.disconnect()
            }
        }

        mediaBrowser = MediaBrowserCompat(this, ComponentName(this, DiloMediaBrowserService::class.java), mediaBrowserConnectionCallback, null)
        mediaBrowser?.connect()

        // 광고 로드
        adManager.loadAd(requestParamBuilder.build())
    }

    /**
     * 샘플 영상 재생 부분 (매체사에서는 이 메소드 코드 참고하지 않으셔도 됩니다)
     */
    private fun playContent() {
        debug("ContentActivity.playContent()")

        // 광고 실행중이면 컨텐츠 재생 무시
        if (adManager.isPlaying) {
            return
        }

        sendBroadcast(contentPlayIntent)

        contentWrapper.visibility = View.VISIBLE
        mediaController?.visibility = View.VISIBLE

        mediaPlayer?.run {
            log("컨텐츠 이어 재생")
            timer.start()
            mediaPlayer?.start()
            this@ContentActivity.isPlaying = true
            return@playContent
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer?.reset()
        mediaPlayer?.setOnCompletionListener {
            val i: Intent = Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            sendBroadcast(i)
            timer.cancel()
            this@ContentActivity.isPlaying = false
        }

        mediaPlayer?.isLooping = false
        surfaceHolder = surfaceView.holder
        surfaceHolder.setKeepScreenOn(true)
        surfaceHolder.addCallback(this)
        // surfaceCreated 강제 호출
        surfaceView.visibility = View.GONE
        surfaceView.visibility = View.VISIBLE
    }

    private val contentPlayIntent: Intent
        get() = Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY)
            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            .putExtra("index", contentIntent.extras!!.getInt("index"))
            .putExtra("item", item)

    override fun onBackPressed() {
        debug("ContentActivity.onBackPressed()")
        try {
            if (isPlaying || mediaPlayer?.isPlaying == true || adManager.isPlaying) {
                val intent: Intent = Intent(this, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            } else {
                super.onBackPressed()
            }
        } catch (e: IllegalStateException) {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        debug("ContentActivity.onStart()")
    }

    override fun onStop() {
        super.onStop()
        debug("ContentActivity.onStop()")
    }

    override fun onPause() {
        super.onPause()
        debug("ContentActivity.onPause()")
    }

    override fun onResume() {
        super.onResume()
        debug("ContentActivity.onResume()")
    }

    /**
     * 딜로 SDK 로부터 액션을 받는 BroadcastReceiver
     */
    var diloActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

            if (action != null) {
                when (action) {
                    // 컴패니언 리로드 액션
                    DiloUtil.ACTION_RELOAD_COMPANION ->
                        if (adManager.reloadCompanion(companionAdView, companionCloseButton)) {
                            adWrapper.visibility = View.VISIBLE
                        }

                    // 사용자의 컴패니언 닫기 액션
                    DiloUtil.ACTION_ON_COMPANION_CLOSED -> log("사용자가 컴패니언을 닫았습니다")

                    // 광고 준비 완료 액션
                    DiloUtil.ACTION_ON_AD_READY -> {

                        log("광고 준비 완료")
                        if (mediaPlayer != null) {
                            log("컨텐츠 일시 중지")
                            timer.cancel()

                            try {
                                mediaPlayer!!.pause()
                            } catch (ignored: IllegalStateException) {
                            }
                        }
                        contentWrapper.visibility = View.INVISIBLE
                        mediaController?.visibility = View.INVISIBLE

                        adManager.start()
                        val i: Intent = Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END)
                            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                        sendBroadcast(i)

                        isPlaying = false
                        adWrapper.visibility = View.VISIBLE

                        log("광고 재생")
                    }

                    // 광고 플레이 시작 액션
                    DiloUtil.ACTION_ON_AD_START -> {
                        skipOffset = 0L
                        val adInfo: AdInfo? = intent.getParcelableExtra(DiloUtil.EXTRA_AD_INFO)
                        val i: Intent = Intent(DiloSampleAppUtil.CONTENT_ACTION_AD)
                            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                            .putExtra("index", intent.extras!!.getInt("index"))
                            .putExtra("item", item)
                            .putExtra("currentSec", 0.0)
                            .putExtra("totalSec", 0.0)
                            .putExtra("adInfo", "${adInfo?.currentOffset}/${adInfo?.totalCount}")
                        sendBroadcast(i)

                        // [광고주] 광고이름
                        adTitle.text = ("[${adInfo?.advertiserName}] ${adInfo?.title}")
                        adCount.visibility = View.VISIBLE

                        log("========================================")
                        log("광고 정보")
                        log("타입     : ${adInfo?.type}")
                        log("광고주   : ${adInfo?.advertiserName}")
                        log("광고명   : ${adInfo?.title}")
                        log("길이     : ${adInfo?.duration}초")
                        log("광고 수  : ${adInfo?.currentOffset}/${adInfo?.totalCount}")
                        log("컴패니언 : ${if (adInfo?.hasCompanion == true) "있음" else "없음"}")
                        log("스킵 " + if (adInfo?.skipOffset != 0L) "가능 ${adInfo?.skipOffset?.toDouble().toTimeString()}" else "불가능")
                        log("========================================")
                        log("재생이 시작되었습니다")
                        skipOffset = adInfo?.skipOffset ?: 0L
                        if (adInfo?.skipOffset == 0L) {
                            skipButton.text = "스킵 불가능"
                            skipButton.visibility = View.VISIBLE
                        }
                        adWrapper.visibility = View.VISIBLE
                    }

                    // 광고 재생 완료 액션 (각각의 광고 재생 완료마다 호출)
                    DiloUtil.ACTION_ON_AD_COMPLETED -> log("재생이 완료되었습니다")

                    // 모든 광고 재생 완료 액션 (가장 마지막 광고 재생 완료 시 한 번 호출)
                    DiloUtil.ACTION_ON_ALL_AD_COMPLETED -> log("모든 광고 재생이 완료되었습니다")

                    // 광고 일시 중지 액션
                    DiloUtil.ACTION_ON_PAUSE -> log("일시중지")

                    // 광고 재개 액션
                    DiloUtil.ACTION_ON_RESUME -> log("재개")

                    // 요청한 조건에 맞는 광고 없음 액션
                    DiloUtil.ACTION_ON_NO_FILL -> {
                        log("광고가 없습니다 (No Fill)")
                        Toast.makeText(this@ContentActivity, "광고가 없습니다 (No Fill)", Toast.LENGTH_SHORT).show()
                        adWrapper.visibility = View.INVISIBLE
                        playContent()

                        // 카울리 배너/전면 광고 요청
                        requestCaulyAd()
                    }

                    // 스킵 가능한 광고일 때 스킵 가능 시점 도달 액션
                    DiloUtil.ACTION_ON_SKIP_ENABLED -> {
                        log("스킵 가능 시점 도달")
                        if (companionAdView.visibility == View.VISIBLE) {
                            skipButton.visibility = View.VISIBLE
                        }
                    }

                    // 에러 발생 액션
                    DiloUtil.ACTION_ON_ERROR -> {
                        val error: DiloError? = intent.getParcelableExtra(DiloUtil.EXTRA_ERROR)
                        log("광고 요청 중 에러가 발생하였습니다\n\t타입: ${error?.type}, 코드 :${error?.code}, 에러: ${error?.error}, 상세: ${error?.detail}")
                        Toast.makeText(
                            this@ContentActivity,
                            "에러 발생\n[${error?.code}] ${error?.error.toString()} [${error?.detail}]",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (error?.code != DiloError.CODE_TOO_MANY_REQUEST) {
                            playContent()
                        }

                        // 카울리 배너/전면 광고 요청
                        requestCaulyAd()
                    }

                    // 광고 진행 사항 업데이트 액션
                    DiloUtil.ACTION_ON_TIME_UPDATE -> {
                        val progress: Progress? = intent.getParcelableExtra(DiloUtil.EXTRA_PROGRESS)

                        val percent: Int = progress?.run {seconds * 100 / duration}?.toInt() ?: 0

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBar.setProgress(percent, false)
                        } else {
                            progressBar.progress = percent
                        }

                        //                        log(
                        //                            String.format(
                        //                                "광고 진행정보 :: %5dms / %5dms [%3d%%]",
                        //                                (progress?.seconds  ?: 0).toDouble().times(1000).toInt(),
                        //                                (progress?.duration ?: 0).toDouble().times(1000).toInt(),
                        //                                percent
                        //                            )
                        //                        )

                        currentTime.text = progress?.seconds.toTimeString()
                        totalTime.text = progress?.duration.toTimeString()
                        adCount.text = ("광고 [${progress?.current}/${progress?.total}]")

                        if (skipOffset != 0L) {
                            var msg: String? = "건너뛰기"
                            val sec: Int = progress?.seconds?.toInt() ?: 0
                            if (skipOffset - sec > 0) {
                                msg = "${skipOffset - sec}초 후 건너뛰기"
                                skipButton.visibility = View.VISIBLE
                            }
                            skipButton.text = msg
                        }
                        val i = Intent(DiloSampleAppUtil.CONTENT_ACTION_AD)
                            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                            .putExtra("index", intent.extras!!.getInt("index"))
                            .putExtra("item", item)
                            .putExtra("currentSec", progress?.seconds)
                            .putExtra("totalSec", progress?.duration)
                            .putExtra("adInfo", "${progress?.current ?: 0}/${progress?.total ?: 0}")
                        sendBroadcast(i)
                    }

                    // 사용자 광고 스킵 액션
                    DiloUtil.ACTION_ON_AD_SKIPPED -> log("사용자가 광고를 건너뛰었습니다")

                    // SDK로부터 메시지 수신
                    DiloUtil.ACTION_ON_MESSAGE -> {
                        val msg: String? = intent.getStringExtra(DiloUtil.EXTRA_MESSAGE)
                        log(msg!!)
                    }

                    // SDK 서비스 종료 액션
                    DiloUtil.ACTION_ON_SVC_DESTROYED -> {
                        log("딜로 SDK 서비스 종료")
                        skipButton.visibility = View.GONE
                        adCount.visibility = View.GONE
                        playContent()
                    }
                }
            }
        }
    }

    fun log(message: String) {
        log.run {
            if (message == CLEAR_LOG) {
                editableText?.clear()
                debug("# 로그 초기화")
                return
            }
            val msg = "# $message\n"
            editableText?.insert(0, msg)
            debug(msg)
        }
    }

    // 카울리 전면/배너 광고 요청 메소드
    fun requestCaulyAd() {
        val fallback: String = prefs.getString(DiloSampleAppUtil.PREF_DILO_NO_ADS_FALLBACK, "NONE")!!.trim()

        when(fallback) {
            "CAULY_INTERSTITIAL" -> {
                // Dilo 광고 없거나 오류시 카울리 전면 광고 Fallback
                caulyInterstitialAd?.let {
                    it.requestInterstitialAd(this)
                    showCaulyInterstitial = true
                }
            }

            "CAULY_BANNER" -> {
                // Dilo 광고 없거나 오류시 카울리 배너 광고 Fallback
                caulyBannerAd = CaulyAdView(this).apply {
                    setAdInfo(CaulyAdInfoBuilder(getString(R.string.cauly_banner_app_code))
                        .bannerHeight(CaulyAdInfoBuilder.FIXED)
                        .setBannerSize(300, 250)
                        .build()
                    )
                    setAdViewListener(this@ContentActivity)

                    val rootView = viewBinding.caulyBanner
                    val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    rootView.removeAllViews()
                    rootView.addView(this, params)
                }
            }

            "NONE" -> {
                log("카울리 대체 광고 무시됨")
            }
        }
    }

    //////////////////////////////
    // 카울리 전면 광고 Callback
    //////////////////////////////
    // 카울리 전면 광고의 경우, 광고 수신 후 자동으로 노출되지 않으므로,
    // 반드시 onReceiveInterstitialAd 메소드에서 노출 처리해 주어야 한다
    override fun onReceiveInterstitialAd(ad: CaulyInterstitialAd?, isChargeableAd: Boolean) {
        log("카울리 전면 광고 대체 실행")

        if (showCaulyInterstitial) {
            ad?.show()

            // n초 후 전면 광고 닫기
//             Handler(Looper.getMainLooper())
//                 .postDelayed({ ad?.cancel() }, n * 1000)
        } else {
            ad?.cancel()
        }
    }

    // 전면 광고 수신 실패할 경우 호출됨
    override fun onFailedToReceiveInterstitialAd(ad: CaulyInterstitialAd?, errorCode: Int, errorMsg: String?) {
        TODO("Not yet implemented")
    }

    // 전면 광고가 닫힌 경우 호출됨
    override fun onClosedInterstitialAd(ad: CaulyInterstitialAd?) {
        showCaulyInterstitial = false
    }

    override fun onLeaveInterstitialAd(ad: CaulyInterstitialAd?) {
        showCaulyInterstitial = false
    }

    //////////////////////////////
    // 카울리 배너 광고 Callback
    //////////////////////////////
    // 광고 동작에 대해 별도 처리가 필요 없는 경우,
    // Activity의 "implements CaulyAdViewListener" 부분 제거하고 생략 가능

    // 광고 수신 성공 & 노출된 경우 호출됨
    override fun onReceiveAd(adView: CaulyAdView?, isChargeableAd: Boolean) {
        log("카울리 배너 광고 대체 실행")
        viewBinding.caulyBannerWrapper.visibility = View.VISIBLE
    }

    // 배너 광고 수신 실패할 경우 호출됨
    override fun onFailedToReceiveAd(adView: CaulyAdView?, errorCode: Int, errorMsg: String?) {
        log("카울리 배너 광고 - [$errorCode] $errorMsg")
        viewBinding.caulyBannerWrapper.visibility = View.GONE
    }

    // 광고 배너를 클릭하여 WebView를 통해 랜딩 페이지가 열린 경우 호출됨
    override fun onShowLandingScreen(adView: CaulyAdView?) {
        TODO("Not yet implemented")
    }

    // 광고 배너를 클릭하여 WebView를 통해 열린 랜딩 페이지가 닫힌 경우 호출됨
    override fun onCloseLandingScreen(adView: CaulyAdView?) {
        TODO("Not yet implemented")
    }
}
