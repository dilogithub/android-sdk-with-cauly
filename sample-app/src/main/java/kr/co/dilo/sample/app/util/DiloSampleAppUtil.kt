package kr.co.dilo.sample.app.util

import android.util.Log
import kr.co.dilo.sample.app.R
import kr.co.dilo.sample.app.ui.SampleApplication

/**
 * 샘플앱에서 사용하는 유틸 클래스
 */
object DiloSampleAppUtil {

    // 컨텐츠 재생 액션
    const val CONTENT_ACTION_PLAY_END    = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY_END"
    const val CONTENT_ACTION_PLAY        = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY"
    const val CONTENT_ACTION_ON_PROGRESS = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_ON_PROGRESS"
    const val CONTENT_ACTION_AD          = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_AD"
    const val CONTENT_ACTION_PLAY_PAUSE  = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY_PAUSE"

    val PREF_DILO_COMPANION_WIDTH: String = SampleApplication.instance.getString(R.string.pref_dilo_companion_width)
    val PREF_DILO_COMPANION_HEIGHT: String = SampleApplication.instance.getString(R.string.pref_dilo_companion_height)
    val PREF_DILO_PRODUCT_TYPE: String = SampleApplication.instance.getString(R.string.pref_dilo_product_type)
    val PREF_DILO_FILL_TYPE: String = SampleApplication.instance.getString(R.string.pref_dilo_fill_type)
    val PREF_DILO_AD_POSITION_TYPE: String = SampleApplication.instance.getString(R.string.pref_dilo_ad_position_type)
    val PREF_DILO_DURATION: String = SampleApplication.instance.getString(R.string.pref_dilo_duration)
    val PREF_DILO_PACKAGE_NAME: String = SampleApplication.instance.getString(R.string.pref_dilo_package_name)
    val PREF_DILO_EPI_CODE: String = SampleApplication.instance.getString(R.string.pref_dilo_epi_code)
    val PREF_DILO_CHANNEL_NAME: String = SampleApplication.instance.getString(R.string.pref_dilo_channel_name)
    val PREF_DILO_EPISODE_NAME: String = SampleApplication.instance.getString(R.string.pref_dilo_episode_name)
    val PREF_DILO_CREATOR_ID: String = SampleApplication.instance.getString(R.string.pref_dilo_creator_id)
    val PREF_DILO_CREATOR_NAME: String = SampleApplication.instance.getString(R.string.pref_dilo_creator_name)
    val PREF_DILO_AD_TARGET: String = SampleApplication.instance.getString(R.string.pref_dilo_ad_target)
    val PREF_DILO_COMPANION_SIZE: String = SampleApplication.instance.getString(R.string.pref_dilo_companion_size)
    val PREF_DILO_USE_PAUSE_IN_NOTIFICATION: String = SampleApplication.instance.getString(R.string.pref_dilo_use_pause_in_notification)
    val PREF_DILO_USE_PROGRESS_BAR_IN_NOTIFICATION: String = SampleApplication.instance.getString(R.string.pref_dilo_use_progress_bar_in_notification)
    val PREF_DILO_NOTIFICATION_TITLE: String = SampleApplication.instance.getString(R.string.pref_dilo_notification_title)
    val PREF_DILO_NOTIFICATION_TEXT: String = SampleApplication.instance.getString(R.string.pref_dilo_notification_text)
    val PREF_DILO_USE_BACKGROUND: String = SampleApplication.instance.getString(R.string.pref_dilo_use_background)
    val PREF_DILO_AD_REQUEST_DELAY: String = SampleApplication.instance.getString(R.string.pref_dilo_ad_request_delay)
    val PREF_DILO_SDK_VERSION: String = SampleApplication.instance.getString(R.string.pref_dilo_sdk_version)
    val PREF_DILO_ALBUM_ART_URI: String = SampleApplication.instance.getString(R.string.pref_dilo_album_art_uri)
    val PREF_DILO_NO_ADS_FALLBACK: String = SampleApplication.instance.getString(R.string.pref_dilo_no_ads_fallback)
}

/**
 * 로그 태그
 */
private const val LOG_TAG = "DILO_SAMPLE_APP"

internal fun verbose(msg: Any) = Log.v(LOG_TAG, msg.toString())
internal fun debug(msg: Any)   = Log.d(LOG_TAG, msg.toString())
internal fun info(msg: Any)    = Log.i(LOG_TAG, msg.toString())
internal fun warn(msg: Any)    = Log.w(LOG_TAG, msg.toString())
internal fun error(msg: Any)   = Log.e(LOG_TAG, msg.toString())

/**
 * String 값을 int 형으로 변환
 * @param value int 형으로 변환할 값
 * @param defValue 에러 발생 시 반환될 기본 값
 * @return value의 변환된 int형 값, 에러 발생 시 defValue 반환
 */
fun String?.safeParseInt(defValue: Int): Int {
    return try {
        this?.toInt() ?: defValue
    } catch (e: Exception) {
        defValue
    }
}

/**
 * SDK 에서 받은 재생 시간(숫자)을 문자로 변경
 * @return 문자열로 변환된 재생 시간 (mm:ss)
 * ```code
 *     ex) second -> 7.000
 *     return 00:07
 * ```
 */
fun Double?.toTimeString(): String {
    val value = this ?: 0
    val ms = (value).toLong().times(1000)
    val min = ms / 1000 / 60 % 60
    val sec = ms / 1000 % 60
    return String.format("%02d:%02d", min, sec)
}
