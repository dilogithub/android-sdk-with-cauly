package kr.co.dilo.sample.app.ui

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.MediaController.MediaPlayerControl
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kr.co.dilo.sample.app.R
import kr.co.dilo.sample.app.databinding.ActivityContentBinding
import kr.co.dilo.sample.app.ui.content.DummyContent
import kr.co.dilo.sample.app.util.DiloSampleAppUtil
import kr.co.dilo.sample.app.util.debug
import kr.co.dilo.sample.app.util.safeParseInt
import kr.co.dilo.sample.app.util.toTimeString
import kr.co.dilo.sdk.AdManager
import kr.co.dilo.sdk.AdView
import kr.co.dilo.sdk.DiloUtil
import kr.co.dilo.sdk.RequestParam
import kr.co.dilo.sdk.model.AdInfo
import kr.co.dilo.sdk.model.DiloError
import kr.co.dilo.sdk.model.Progress

/**
 * 광고와 컨텐츠(샘플 영상)을 보여주는 액티비티
 */
class ContentActivity : AppCompatActivity(), SurfaceHolder.Callback {

    // Content
    private var contentWrapper: ViewGroup? = null
    private var surfaceView: SurfaceView? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var mediaController: MediaController? = null
    private var timer: CountDownTimer? = null

    // Ad
    private var companionAdView: AdView? = null
    private var adWrapper: ViewGroup? = null
    private var skipButton: Button? = null
    private var adManager: AdManager? = null

    // Ad 제어 View
    private var play: Button? = null
    private var pause: Button? = null
    private var reload: Button? = null
    private var release: Button? = null
    private var settings: Button? = null
    private var companionCloseButton: ViewGroup? = null

    // Ad 정보 View
    private var progressBar: ProgressBar? = null
    private var currentTime: TextView? = null
    private var totalTime: TextView? = null
    private var adTitle: TextView? = null
    private var adCount: TextView? = null
    private var playInfoWrapper: ViewGroup? = null

    private var contentIntent: Intent? = null
    private var item: DummyContent.DummyItem? = null
    private var skipOffset: Long = 0
    private var isPlaying: Boolean = false
    private var isMediaControllerShowing: Boolean = false
    private var prefs: SharedPreferences? = null
    private var viewBinding: ActivityContentBinding? = null
    private var log: EditText? = null

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
        setContentView(viewBinding?.root)

        prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        contentIntent = intent
        adManager = SampleApplication.instance.adManager
        contentWrapper = viewBinding?.contentWrapper

        log = viewBinding?.logArea
        log?.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("로그를 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    log?.editableText?.clear()
                }
                .setNegativeButton("취소", null)
                .show()
            false
        }

        companionAdView = viewBinding?.companionAdView
        adWrapper = viewBinding?.adWrapper
        skipButton = viewBinding?.skipButton
        play = viewBinding?.play
        pause = viewBinding?.pause
        reload = viewBinding?.reload
        release = viewBinding?.release
        settings = viewBinding?.settings
        surfaceView = viewBinding?.videoView
        companionCloseButton = viewBinding?.companionCloseButton
        progressBar = viewBinding?.progressBar
        currentTime = viewBinding?.currentTime
        adTitle = viewBinding?.adTitle
        totalTime = viewBinding?.totalTime
        adCount = viewBinding?.adCount
        playInfoWrapper = viewBinding?.playInfo
        play?.setOnClickListener {
            // 로그 초기화
            log(CLEAR_LOG)
            val adRequestDelay: Int =
                prefs!!.getString(DiloSampleAppUtil.PREF_DILO_AD_REQUEST_DELAY, "0").safeParseInt(-1)
            if (adRequestDelay > 0) {
                val msg = "$adRequestDelay 초 요청 지연"
                log(msg)
                Toast.makeText(this@ContentActivity, msg, Toast.LENGTH_SHORT).show()
            }
            Handler(Looper.getMainLooper())
                .postDelayed({ onAdRequest() }, adRequestDelay * 1000L)
        }

        skipButton?.setOnClickListener {
            adManager?.skip()
        }

        pause?.setOnClickListener {
            adManager?.run {
                playOrPause()
                sendBroadcast(
                    Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_PAUSE)
                        .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                )
            }
        }

        reload?.setOnClickListener {
            if (adManager?.reloadCompanion(companionAdView!!, companionCloseButton) == true) {
                log("컴패니언 리로드")
                adWrapper?.visibility = View.VISIBLE
            }
        }

        release?.setOnClickListener {
            adManager?.release()
            adWrapper?.visibility = View.INVISIBLE
            playContent()
        }

        settings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        surfaceView?.setOnClickListener {
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

        contentWrapper?.visibility = View.INVISIBLE
        val receiverIntent = Intent(application, diloActionReceiver.javaClass)
        val receiverPendingIntent: PendingIntent? = PendingIntent.getBroadcast(application, 0, receiverIntent, PendingIntent.FLAG_NO_CREATE)
        if (receiverPendingIntent == null) {
            application.registerReceiver(diloActionReceiver, DiloUtil.DILO_INTENT_FILTER)
        }
        
        adWrapper?.visibility = View.INVISIBLE
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
                timer?.cancel()
                timer = object : CountDownTimer(duration.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val currentSec: Double = mediaPlayer!!.currentPosition.toDouble() / 1000
                        val totalSec: Double = duration.toDouble() / 1000
                        val i: Intent = Intent(DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS)
                            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                            .putExtra("item", item)
                            .putExtra("currentSec", currentSec)
                            .putExtra("totalSec", totalSec)
                        sendBroadcast(i)

                        currentTime?.setText(currentSec.toTimeString(), TextView.BufferType.NORMAL)
                        totalTime?.setText(totalSec.toTimeString(), TextView.BufferType.NORMAL)

                        item = intent.getParcelableExtra("item")
                        adTitle?.text = ("${item?.title} - ${item?.desc}")

                        val percent: Int = (currentSec * 100 / totalSec).toInt()
                        progressBar?.progress = percent

                    }

                    override fun onFinish() {}
                }
                log("컨텐츠 재생")
                sendBroadcast(contentPlayIntent)
                this@ContentActivity.isPlaying = true
                timer?.start()
                start()
            }

            setOnErrorListener { _: MediaPlayer?, _: Int, _: Int -> false }

            mediaController!!.setMediaPlayer(object : MediaPlayerControl {
                override fun start() {
                    log("컨텐츠 재생")
                    timer?.start()
                    this@mediaPlayer.start()
                }

                override fun pause() {
                    timer?.cancel()
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
            mediaController!!.setAnchorView(surfaceView)
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
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nPrefs = prefs!!
        // SharedPreferences 에서 값 읽어 옴(테스트 용)
        val epiCode:     String = nPrefs.getString(DiloSampleAppUtil.PREF_DILO_EPI_CODE, "")!!.trim()
        val bundleId:    String = nPrefs.getString(DiloSampleAppUtil.PREF_DILO_PACKAGE_NAME, "")!!.trim()
        val channelName: String = nPrefs.getString(DiloSampleAppUtil.PREF_DILO_CHANNEL_NAME, "")!!.trim()
        val episodeName: String = nPrefs.getString(DiloSampleAppUtil.PREF_DILO_EPISODE_NAME, "")!!.trim()
        val creatorId:   String = nPrefs.getString(DiloSampleAppUtil.PREF_DILO_CREATOR_ID, "")!!.trim()
        val creatorName: String = nPrefs.getString(DiloSampleAppUtil.PREF_DILO_CREATOR_NAME, "")!!.trim()
        val duration:       Int = nPrefs.getString(DiloSampleAppUtil.PREF_DILO_DURATION, "15").safeParseInt(-1)

        val productType: RequestParam.ProductType = RequestParam.ProductType.valueOf(
                nPrefs.getString(DiloSampleAppUtil.PREF_DILO_PRODUCT_TYPE, RequestParam.ProductType.DILO_PLUS.value.uppercase())!!
        )
        val fillType: RequestParam.FillType = RequestParam.FillType.valueOf(
                nPrefs.getString(DiloSampleAppUtil.PREF_DILO_FILL_TYPE, RequestParam.FillType.MULTI.value.uppercase())!!
        )
        val adPositionType: RequestParam.AdPositionType = RequestParam.AdPositionType.valueOf(
                nPrefs.getString(DiloSampleAppUtil.PREF_DILO_AD_POSITION_TYPE, RequestParam.AdPositionType.PRE.value.uppercase())!!
        )

        val usePauseInNotification:       Boolean = nPrefs.getBoolean(DiloSampleAppUtil.PREF_DILO_USE_PAUSE_IN_NOTIFICATION, true)
        val useProgressBarInNotification: Boolean = nPrefs.getBoolean(DiloSampleAppUtil.PREF_DILO_USE_PROGRESS_BAR_IN_NOTIFICATION, true)

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
            companionAdView(companionAdView!!)            // Companion View 설정 (Companion 이 있는 광고가 나가려면 필수)
            closeButton(companionCloseButton)             // 닫기 버튼 설정
            skipButton(skipButton)                        // Skip 버튼 설정
            notificationContentIntent(notificationIntent) // Notification Click PendingIntent 설정
            notificationContentTitle(                     // Notification Title 설정 (상단 텍스트)
                nPrefs.getString(DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TITLE,"")
            )
            notificationContentText(                      // Notification Text 설정 (하단 텍스트)
                nPrefs.getString(DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TEXT,"")
            )

            // 컴패니언 사이즈 수동 설정
            if (nPrefs.getBoolean(DiloSampleAppUtil.PREF_DILO_COMPANION_SIZE, false)) {
                companionSize(
                    nPrefs.getString(DiloSampleAppUtil.PREF_DILO_COMPANION_WIDTH, "300").safeParseInt(300),
                    nPrefs.getString(DiloSampleAppUtil.PREF_DILO_COMPANION_HEIGHT, "300").safeParseInt(300)
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

        // 광고 로드
        adManager?.loadAd(requestParamBuilder.build())
    }

    /**
     * 샘플 영상 재생 부분 (매체사에서는 이 메소드 코드 참고하지 않으셔도 됩니다)
     */
    private fun playContent() {
        debug("ContentActivity.playContent()")
        sendBroadcast(contentPlayIntent)

        contentWrapper?.visibility = View.VISIBLE
        mediaController?.visibility = View.VISIBLE

        mediaPlayer?.run {
            log("컨텐츠 이어 재생")
            timer?.start()
            mediaPlayer!!.start()
            this@ContentActivity.isPlaying = true
            return@playContent
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer?.reset()
        mediaPlayer?.setOnCompletionListener {
            val i: Intent = Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            sendBroadcast(i)
            timer?.cancel()
            this@ContentActivity.isPlaying = false
        }

        mediaPlayer?.isLooping = false
        surfaceHolder = surfaceView!!.holder
        surfaceHolder?.setKeepScreenOn(true)
        surfaceHolder?.addCallback(this)
        // surfaceCreated 강제 호출
        surfaceView?.visibility = View.GONE
        surfaceView?.visibility = View.VISIBLE
    }

    private val contentPlayIntent: Intent
        get() = Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY)
            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            .putExtra("index", contentIntent!!.extras!!.getInt("index"))
            .putExtra("item", item)

    override fun onBackPressed() {
        debug("ContentActivity.onBackPressed()")
        try {
            if (isPlaying || mediaPlayer?.isPlaying == true || adManager?.isPlaying == true) {
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
        debug("ContentActivity.onStart()")
        super.onStart()
    }

    override fun onStop() {
        debug("ContentActivity.onStop()")
        super.onStop()
    }

    override fun onPause() {
        debug("ContentActivity.onPause()")
        super.onPause()
    }

    override fun onResume() {
        debug("ContentActivity.onResume()")
        super.onResume()
    }

    /**
     * 딜로 SDK 로부터 액션을 받는 BroadcastReceiver
     */
    var diloActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

            if (action != null) {
                when (action) {
                    DiloUtil.ACTION_RELOAD_COMPANION ->
                        if (adManager?.reloadCompanion(companionAdView!!, companionCloseButton) == true) {
                            adWrapper?.visibility = View.VISIBLE
                        }

                    DiloUtil.ACTION_ON_COMPANION_CLOSED -> log("사용자가 컴패니언을 닫았습니다")

                    DiloUtil.ACTION_ON_AD_READY -> {
                        log("광고 준비 완료")
                        if (mediaPlayer != null) {
                            log("컨텐츠 일시 중지")
                            timer?.cancel()

                            try {
                                mediaPlayer!!.pause()
                            } catch (ignored: IllegalStateException) {
                            }
                        }
                        contentWrapper?.visibility = View.INVISIBLE
                        mediaController?.visibility = View.INVISIBLE

                        adManager?.start()
                        val i: Intent = Intent(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END)
                            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                        sendBroadcast(i)

                        isPlaying = false
                        adWrapper?.visibility = View.VISIBLE
                        log("광고 재생")
                    }

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
                        adTitle?.text = ("[${adInfo?.advertiserName}] ${adInfo?.title}")
                        adCount?.visibility = View.VISIBLE

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
                            skipButton?.text = "스킵 불가능"
                            skipButton?.visibility = View.VISIBLE
                        }
                        adWrapper?.visibility = View.VISIBLE
                    }

                    DiloUtil.ACTION_ON_AD_COMPLETED -> log("재생이 완료되었습니다")

                    DiloUtil.ACTION_ON_ALL_AD_COMPLETED -> log("모든 광고 재생이 완료되었습니다")

                    DiloUtil.ACTION_ON_PAUSE -> log("일시중지")

                    DiloUtil.ACTION_ON_RESUME -> log("재개")

                    DiloUtil.ACTION_ON_NO_FILL -> {
                        log("광고가 없습니다 (No Fill)")
                        Toast.makeText(this@ContentActivity, "광고가 없습니다 (No Fill)", Toast.LENGTH_SHORT).show()
                        adWrapper?.visibility = View.INVISIBLE
                        playContent()
                    }

                    DiloUtil.ACTION_ON_SKIP_ENABLED -> {
                        log("스킵 가능 시점 도달")
                        if (companionAdView?.visibility == View.VISIBLE) {
                            skipButton?.visibility = View.VISIBLE
                        }
                    }

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
                    }

                    DiloUtil.ACTION_ON_TIME_UPDATE -> {
                        val progress: Progress? = intent.getParcelableExtra(DiloUtil.EXTRA_PROGRESS)

                        val percent: Int = progress?.run {seconds * 100 / duration}?.toInt() ?: 0

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBar?.setProgress(percent, false)
                        } else {
                            progressBar?.progress = percent
                        }

//                        log(
//                            String.format(
//                                "광고 진행정보 :: %5dms / %5dms [%3d%%]",
//                                (progress?.seconds  ?: 0).toDouble().times(1000).toInt(),
//                                (progress?.duration ?: 0).toDouble().times(1000).toInt(),
//                                percent
//                            )
//                        )

                        currentTime?.text = progress?.seconds.toTimeString()
                        totalTime?.text = progress?.duration.toTimeString()
                        adCount?.text = ("광고 [${progress?.current}/${progress?.total}]")

                        if (skipOffset != 0L) {
                            var msg: String? = "건너뛰기"
                            val sec: Int = progress?.seconds?.toInt() ?: 0
                            if (skipOffset - sec > 0) {
                                msg = "${skipOffset - sec}초 후 건너뛰기"
                                skipButton?.visibility = View.VISIBLE
                            }
                            skipButton?.text = msg
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

                    DiloUtil.ACTION_ON_AD_SKIPPED -> log("사용자가 광고를 건너뛰었습니다")

                    DiloUtil.ACTION_ON_MESSAGE -> {
                        val msg: String? = intent.getStringExtra(DiloUtil.EXTRA_MESSAGE)
                        log(msg!!)
                    }

                    DiloUtil.ACTION_ON_SVC_DESTROYED -> {
                        log("딜로 SDK 서비스 종료")
                        skipButton?.visibility = View.GONE
                        adCount?.visibility = View.GONE
                        playContent()
                    }
                }
            }
        }
    }

    fun log(message: String) {
        log?.run {
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
}
