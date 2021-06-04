package kr.co.dilo.sample.app.util;

import android.annotation.SuppressLint;
import kr.co.dilo.sample.app.R;
import kr.co.dilo.sample.app.SampleApplication;

/**
 * 샘플앱에서 사용하는 유틸 클래스
 */
public class DiloSampleAppUtil {

  /**
   * 로그 태그
   */
  public static final String LOG_TAG = "DILO_SAMPLE_APP";

  // 컨텐츠 재생 액션
  public static final String CONTENT_ACTION_PLAY_END    = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY_END";
  public static final String CONTENT_ACTION_PLAY        = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY";
  public static final String CONTENT_ACTION_ON_PROGRESS = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_ON_PROGRESS";
  public static final String CONTENT_ACTION_AD          = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_AD";
  public static final String CONTENT_ACTION_PLAY_PAUSE  = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY_PAUSE";
  public static final String CONTENT_ACTION_LOG         = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_LOG";

  public static final String PREF_DILO_COMPANION_WIDTH                  = SampleApplication.getInstance().getString(R.string.pref_dilo_companion_width);
  public static final String PREF_DILO_COMPANION_HEIGHT                 = SampleApplication.getInstance().getString(R.string.pref_dilo_companion_height);
  public static final String PREF_DILO_PRODUCT_TYPE                     = SampleApplication.getInstance().getString(R.string.pref_dilo_product_type);
  public static final String PREF_DILO_FILL_TYPE                        = SampleApplication.getInstance().getString(R.string.pref_dilo_fill_type);
  public static final String PREF_DILO_AD_POSITION_TYPE                 = SampleApplication.getInstance().getString(R.string.pref_dilo_ad_position_type);
  public static final String PREF_DILO_DURATION                         = SampleApplication.getInstance().getString(R.string.pref_dilo_duration);
  public static final String PREF_DILO_PACKAGE_NAME                     = SampleApplication.getInstance().getString(R.string.pref_dilo_package_name);
  public static final String PREF_DILO_EPI_CODE                         = SampleApplication.getInstance().getString(R.string.pref_dilo_epi_code);
  public static final String PREF_DILO_CHANNEL_NAME                     = SampleApplication.getInstance().getString(R.string.pref_dilo_channel_name);
  public static final String PREF_DILO_EPISODE_NAME                     = SampleApplication.getInstance().getString(R.string.pref_dilo_episode_name);
  public static final String PREF_DILO_CREATOR_ID                       = SampleApplication.getInstance().getString(R.string.pref_dilo_creator_id);
  public static final String PREF_DILO_CREATOR_NAME                     = SampleApplication.getInstance().getString(R.string.pref_dilo_creator_name);
  public static final String PREF_DILO_AD_TARGET                        = SampleApplication.getInstance().getString(R.string.pref_dilo_ad_target);
  public static final String PREF_DILO_COMPANION_SIZE                   = SampleApplication.getInstance().getString(R.string.pref_dilo_companion_size);
  public static final String PREF_DILO_USE_PAUSE_IN_NOTIFICATION        = SampleApplication.getInstance().getString(R.string.pref_dilo_use_pause_in_notification);
  public static final String PREF_DILO_USE_PROGRESS_BAR_IN_NOTIFICATION = SampleApplication.getInstance().getString(R.string.pref_dilo_use_progress_bar_in_notification);
  public static final String PREF_DILO_NOTIFICATION_TITLE               = SampleApplication.getInstance().getString(R.string.pref_dilo_notification_title);
  public static final String PREF_DILO_NOTIFICATION_TEXT                = SampleApplication.getInstance().getString(R.string.pref_dilo_notification_text);
  public static final String PREF_DILO_USE_BACKGROUND                   = SampleApplication.getInstance().getString(R.string.pref_dilo_use_background);
  public static final String PREF_DILO_AD_REQUEST_DELAY                 = SampleApplication.getInstance().getString(R.string.pref_dilo_ad_request_delay);
  public static final String PREF_DILO_SDK_VERSION                      = SampleApplication.getInstance().getString(R.string.pref_dilo_sdk_version);

  /**
   * SDK 에서 받은 재생 시간(숫자)을 문자로 변경
   * @param second SDK 에서 받은 재생 시간
   * @return 문자열로 변환된 재생 시간 (mm:ss)
   * <pre>
   *  ex) second -> 7.000<br>
   *         return 00:07
   * </pre>
   */
  @SuppressLint("DefaultLocale")
  public static String secondsToTimeString(double second) {
    long ms = (long) (second * 1000);
    long min = ((ms / 1000 / 60) % 60);
    long sec = ((ms / 1000) % 60);

    return String.format("%02d:%02d", min, sec);
  }

  /**
   * String 값을 int 형으로 변환
   * @param value int 형으로 변환할 값
   * @param defValue 에러 발생 시 반환될 기본 값
   * @return value의 변환된 int형 값, 에러 발생 시 defValue 반환
   */
  public static int safeParseInt(String value, int defValue) {
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      return defValue;
    }
  }
}
