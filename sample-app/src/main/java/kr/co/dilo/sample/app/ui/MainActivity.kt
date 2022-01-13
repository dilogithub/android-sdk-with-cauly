package kr.co.dilo.sample.app.ui

import android.app.PendingIntent
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.dilo.sample.app.R
import kr.co.dilo.sample.app.databinding.ActivityMainBinding
import kr.co.dilo.sample.app.ui.content.ContentRecyclerViewAdapter
import kr.co.dilo.sample.app.ui.content.DummyContent
import kr.co.dilo.sample.app.util.DiloSampleAppUtil
import kr.co.dilo.sample.app.util.debug
import kr.co.dilo.sample.app.util.toTimeString
import kr.co.dilo.sdk.AdManager
import kr.co.dilo.sdk.DiloUtil
import kotlin.system.exitProcess

/**
 * 메인 화면
 */
class MainActivity : AppCompatActivity() {

    /**
     * 광고나 오디오 재생 시 화면 하단에 뜨는 재생 화면 창 (검정색 배경)
     */
    private lateinit var floatingContent: ViewGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var contentStat: TextView
    private lateinit var contentIntent: Intent
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        debug("MainActivity.onCreate()")
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // contentActionReceiver 가 등록되어있지 않다면 등록
        val receiverIntent = Intent(this, contentActionReceiver.javaClass)
        val receiverPendingIntent = PendingIntent.getBroadcast(this, 0, receiverIntent, DiloUtil.setPendingIntentFlagsWithImmutableFlag(PendingIntent.FLAG_NO_CREATE))
        if (receiverPendingIntent == null) {
            val filter = IntentFilter().apply {
                addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY_END)
                addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY_PAUSE)
                addAction(DiloSampleAppUtil.CONTENT_ACTION_PLAY)
                addAction(DiloSampleAppUtil.CONTENT_ACTION_AD)
                addAction(DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS)
            }
            registerReceiver(contentActionReceiver, filter)
        }

        floatingContent = viewBinding.floatingContent
        progressBar = viewBinding.progressBar
        contentStat = viewBinding.contentStat
        contentIntent = Intent(this, ContentActivity::class.java)
        floatingContent.setOnClickListener {
            contentIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(contentIntent)
        }

        val listView: RecyclerView = viewBinding.list
        viewBinding.homeListRefreshLayout.apply {
            setOnRefreshListener {
                listView.y = -5000f
                listView.animate()
                    .y(0f)
                    .setDuration(1000)
                    .start()

                Handler(Looper.getMainLooper())
                    .postDelayed({ isRefreshing = false }, 1000)
            }
        }
        listView.layoutManager = LinearLayoutManager(this)

        // 어댑터 설정
        val items: MutableList<DummyContent.DummyItem> = ArrayList()

        repeat(15) {
            items.add(
                DummyContent.DummyItem(R.drawable.content_image, "딜로", "오디오 광고는 딜로! 에피소드 ${it+1}")
            )
        }
        val adapter = ContentRecyclerViewAdapter(items).apply {
            setOnContentClickListener { pos, item ->
                val intent: Intent = Intent(this@MainActivity, ContentActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .putExtra("item", item)
                    .putExtra("index", pos)
                startActivityForResult(intent, pos)
            }
        }

        listView.adapter = adapter
    }

    override fun onDestroy() {
        debug("MainActivity.onDestroy()")
        super.onDestroy()
        try {
            unregisterReceiver(contentActionReceiver)
        } catch (e: IllegalArgumentException) {}

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // 앱 강제 종료 허용 여부에 따라 서비스 및 프로세스 종료
        if (!prefs.getBoolean(DiloSampleAppUtil.PREF_DILO_USE_BACKGROUND, true)) {
            val adManager: AdManager = SampleApplication.instance.adManager
            adManager.release()
            Handler(Looper.getMainLooper())
                .postDelayed({ exitApplication() }, 500)
        }
    }

    override fun onStart() {
        debug("MainActivity.onStart()")
        super.onStart()
    }

    override fun onStop() {
        debug("MainActivity.onStop()")
        super.onStop()
    }

    /**
     * 뒤로가기 키 눌렀을 때 처리
     */
    override fun onBackPressed() {
        debug("MainActivity.onBackPressed()")
        AlertDialog.Builder(this)
            .setMessage("종료하시겠습니까?")
            .setPositiveButton("종료") { _, _ -> finishAffinity() }
            .setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
            .show()
    }

    override fun onResume() {
        debug("MainActivity.onResume()")
        super.onResume()
    }

    override fun onPause() {
        debug("MainActivity.onPause()")
        super.onPause()
    }

    /**
     * 컨텐츠 액션 수신 리시버
     */
    private var contentActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            when (action) {
                DiloSampleAppUtil.CONTENT_ACTION_PLAY_END ->
                    floatingContent.visibility = View.GONE

                DiloSampleAppUtil.CONTENT_ACTION_PLAY -> {
                    setFloatingContent(intent)
                    contentStat.text = "재생중"
                    floatingContent.visibility = View.VISIBLE
                }

                DiloSampleAppUtil.CONTENT_ACTION_AD -> {
                    setFloatingContent(intent)
                    val adInfo = intent.getStringExtra("adInfo")
                    val current = intent.getDoubleExtra("currentSec", 0.0)
                    val total = intent.getDoubleExtra("totalSec", 0.0)
                    progressBar.progress = (current * 100 / total).toInt()
                    contentStat.text = "광고중 ${adInfo}\n${current.toTimeString()} / ${total.toTimeString()}"
                    floatingContent.visibility = View.VISIBLE
                }

                DiloSampleAppUtil.CONTENT_ACTION_ON_PROGRESS -> {
                    setFloatingContent(intent)
                    val current = intent.getDoubleExtra("currentSec", 0.0)
                    val total = intent.getDoubleExtra("totalSec", 0.0)
                    progressBar.progress = (current * 100 / total).toInt()
                    contentStat.text = "재생중\n${current.toTimeString()} / ${total.toTimeString()}"
                    floatingContent.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 재생 화면 처리
     * @param intent 재생 데이터가 있는 인텐트
     */
    private fun setFloatingContent(intent: Intent?) {
        intent?.run {
            val desc: TextView = viewBinding.contentSmallDesc
            val item: DummyContent.DummyItem? = getParcelableExtra("item")
            contentIntent.putExtra("index", extras!!.getInt("index"))
                .putExtra("item", item)

            desc.text = item?.desc

            val title: TextView = viewBinding.contentSmallTitle
            title.text = item?.title

            val image: ImageView = viewBinding.contentSmallImage
            item?.image?.let { image.setImageResource(it) }
        }
    }

    /**
     * 어플리케이션 (프로세스) 종료
     */
    private fun exitApplication() {
        debug("Exiting Application")
        ActivityCompat.finishAffinity(this)
        exitProcess(0)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        overridePendingTransition(0, R.anim.slide_down_exit)
    }
}
