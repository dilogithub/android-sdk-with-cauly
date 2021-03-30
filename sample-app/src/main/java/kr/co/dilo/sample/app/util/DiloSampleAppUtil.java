package kr.co.dilo.sample.app.util;

import androidx.annotation.NonNull;

public class DiloSampleAppUtil {

  public static final String LOG_TAG = "DILO_SAMPLE_APP";

  public static final String CONTENT_ACTION_PLAY_END = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY_END";
  public static final String CONTENT_ACTION_PLAY = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY";
  public static final String CONTENT_ACTION_ON_PROGRESS = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_ON_PROGRESS";
  public static final String CONTENT_ACTION_AD = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_AD";
  public static final String CONTENT_ACTION_PLAY_PAUSE = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_PLAY_PAUSE";
  public static final String CONTENT_ACTION_LOG = "kr.co.dilo.sdk.sample.app.CONTENT_ACTION_LOG";

  public static String secondsToTimeString(@NonNull double second) {
    long milis = (long) (second * 1000);
    long hours = (milis / 1000 / 60 / 60);
    long minuts = ((milis/ 1000 / 60) % 60);
    long seconds = ((milis / 1000) % 60);
//    long miliseconds = (milis % 1000);

    return String.format("%02d:%02d", minuts, seconds);

  }

}
