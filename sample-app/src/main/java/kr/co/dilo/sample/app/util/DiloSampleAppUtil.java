package kr.co.dilo.sample.app.util;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextUtils;

import java.util.List;

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
   * 호출 URL 에 대한 브라우저를 호출하는 메소드
   *
   * @param context Context 객체
   * @param url     호출 URL
   */
  public static void openBrowser(Context context, String url) {
    Intent intent;

    try {
      intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      intent = selectBrowserIntent(context, intent, url);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addCategory(Intent.CATEGORY_BROWSABLE);
      intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getApplicationContext().getPackageName());
      context.startActivity(intent);
    } catch (Throwable e) {
      intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addCategory(Intent.CATEGORY_BROWSABLE);
      intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getApplicationContext().getPackageName());
      context.startActivity(intent);
    }
  }

  /**
   * 호출 URL 에 대한 브라우저 PendingIntent를 반환하는 메소드
   *
   * @param context Context 객체
   * @param url     호출 URL
   * @return PendingIntent 객체
   */
  public static PendingIntent browserPendingIntent(Context context, String url) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

    try {
      intent = selectBrowserIntent(context, intent, url);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addCategory(Intent.CATEGORY_BROWSABLE);
      intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getApplicationContext().getPackageName());
      return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    } catch (Throwable e) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addCategory(Intent.CATEGORY_BROWSABLE);
      intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getApplicationContext().getPackageName());
      return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
  }

  /**
   * 디바이스에 설치된 브라우저를 확인하여 호출할 수 있는 Intent 를 반환하는 메소드
   *
   * @param context Context 객체
   * @param intent  Intent 객체
   * @param url     호출 URL
   * @return 선택된 브라우저 호출 Intent
   */
  private static Intent selectBrowserIntent(Context context, Intent intent, String url) {
    PackageManager pm = context.getPackageManager();

    if (pm != null && url.startsWith("http")) {
      String className = getMainActivity(context, "com.android.browser");

      if (!TextUtils.isEmpty(className)) {
        intent.setClassName("com.android.browser", className);
      } else {
        String className2 = getMainActivity(context, "com.android.chrome");
        if (!TextUtils.isEmpty(className2)) {
          intent.setClassName("com.android.chrome", className2);
        }
      }
    }

    return intent;
  }
  /**
   * 패키지 이름에 해당하는 메인 클래스 이름을 반환하는 메소드
   *
   * @param context     Context 객체
   * @param packageName 패키지 이름
   * @return Main 액티비티 이름
   */
  private static String getMainActivity(Context context, String packageName) {
    if (packageName == null || packageName.equals("")) {
      return "";
    }

    try {
      Intent i = new Intent(Intent.ACTION_MAIN);
      i.setPackage(packageName);

      List<ResolveInfo> groupApps = context.getPackageManager().queryIntentActivities(i, PackageManager.GET_RESOLVED_FILTER);
      if (groupApps == null) {
        return "";
      }
      for (ResolveInfo info : groupApps) {
        return info.activityInfo.name;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return "";
  }

}
