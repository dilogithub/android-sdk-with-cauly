package kr.co.dilo.sample.app.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import androidx.preference.*;
import kr.co.dilo.sample.app.R;
import kr.co.dilo.sdk.DiloUtil;

/**
 * 설정 화면
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences  prefs;
    EditTextPreference companionWidth;
    EditTextPreference companionHeight;
    ListPreference     productType;
    ListPreference     fillType;
    ListPreference     adPositionType;
    EditTextPreference duration;
    EditTextPreference packageName;
    EditTextPreference epiCode;
    EditTextPreference channelName;
    EditTextPreference episodeName;
    EditTextPreference creatorId;
    EditTextPreference creatorName;
    SwitchPreference   target;
    SwitchPreference   companionSize;
    SwitchPreference   usePauseInNotification;
    SwitchPreference   useProgressBarInNotification;
    EditTextPreference notificationTitle;
    EditTextPreference notificationText;
    SwitchPreference   useBackground;
    EditTextPreference adRequestDelay;
    Preference         sdkVersion;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);

        companionWidth               = findPreference("companion_width");
        companionHeight              = findPreference("companion_height");
        productType                  = findPreference("product_type");
        fillType                     = findPreference("fill_type");
        adPositionType               = findPreference("ad_position_type");
        duration                     = findPreference("duration");
        packageName                  = findPreference("package_name");
        epiCode                      = findPreference("epi_code");
        channelName                  = findPreference("channel_name");
        episodeName                  = findPreference("episode_name");
        creatorId                    = findPreference("creator_id");
        creatorName                  = findPreference("creator_name");
        target                       = findPreference("target");
        companionSize                = findPreference("companion_size");
        usePauseInNotification       = findPreference("use_pause_in_notification");
        useProgressBarInNotification = findPreference("use_progress_bar_in_notification");
        notificationTitle            = findPreference("notification_title");
        notificationText             = findPreference("notification_text");
        useBackground                = findPreference("use_background");
        adRequestDelay               = findPreference("ad_request_delay");
        sdkVersion                   = findPreference("sdk_version");

        if (sdkVersion != null) {
            sdkVersion.setSummary(DiloUtil.getSDKVersion());
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // 초기값 설정
        if (!prefs.getString("companion_width", "").equals("")) {
            companionWidth.setSummary(prefs.getString("companion_width", "300"));
        }

        if (!prefs.getString("companion_height", "").equals("")) {
            companionHeight.setSummary(prefs.getString("companion_height", "300"));
        }

        if (!prefs.getString("product_type", "").equals("")) {
            productType.setSummary(prefs.getString("product_type", "DILO_PLUS_ONLY"));
        }

        if (!prefs.getString("fill_type", "").equals("")) {
            fillType.setSummary(prefs.getString("fill_type", "MULTI"));
        }

        if (!prefs.getString("ad_position_type", "").equals("")) {
            adPositionType.setSummary(prefs.getString("ad_position_type", "PRE"));
        }

        if (!prefs.getString("duration", "").equals("")) {
            duration.setSummary(prefs.getString("duration", "15"));
        }

        if (!prefs.getString("package_name", "").equals("")) {
            packageName.setSummary(prefs.getString("package_name", "300"));
        }

        if (!prefs.getString("epi_code", "").equals("")) {
            epiCode.setSummary(prefs.getString("epi_code", "300"));
        }

        if (!prefs.getString("channel_name", "").equals("")) {
            channelName.setSummary(prefs.getString("channel_name", "테스트 채널"));
        }

        if (!prefs.getString("episode_name", "").equals("")) {
            episodeName.setSummary(prefs.getString("episode_name", "테스트 에피소드"));
        }

        if (!prefs.getString("creator_id", "").equals("")) {
            creatorId.setSummary(prefs.getString("creator_id", "tester"));
        }

        if (!prefs.getString("creator_name", "").equals("")) {
            creatorName.setSummary(prefs.getString("creator_name", "테스터"));
        }

        if (!prefs.getString("notification_title", "").equals("")) {
            notificationTitle.setSummary(prefs.getString("notification_title", getString(R.string.app_name)));
        }

        if (!prefs.getString("notification_text", "").equals("")) {
            notificationText.setSummary(prefs.getString("notification_text", getString(R.string.app_name) + " 후원하는 광고 재생 중"));
        }

        if (!prefs.getString("ad_request_delay", "").equals("")) {
            adRequestDelay.setSummary(prefs.getString("ad_request_delay", "0"));
        }

        if (target != null) {
            target.setSummary(DiloUtil.getAdUrl(prefs.getBoolean("target", false)));
            target.setTitle(prefs.getBoolean("target", false) ? "테스트 서버" : "운영 서버");
        }

        boolean autoCompanionSize = prefs.getBoolean("companion_size", false);
        companionWidth.setEnabled(!autoCompanionSize);
        companionHeight.setEnabled(!autoCompanionSize);
        companionSize.setSummary(autoCompanionSize ? "자동" : "수동 설정");

        prefs.registerOnSharedPreferenceChangeListener(this);

        // 숫자 유형만 받도록 설정
        companionWidth.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        companionHeight.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        duration.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER));
        adRequestDelay.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
    }

    /**
     * 설정값 변경 리스너
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "target":
                target.setTitle(prefs.getBoolean("target", false) ? "테스트 서버" : "운영 서버");
                target.setSummary(DiloUtil.getAdUrl(prefs.getBoolean("target", false)));
                break;

            case "companion_width":
                companionWidth.setSummary(prefs.getString("companion_width", "300"));
                break;

            case "companion_height":
                companionHeight.setSummary(prefs.getString("companion_height", "300"));
                break;

            case "product_type":
                productType.setSummary(prefs.getString("product_type", "DILO_PLUS_ONLY"));
                break;

            case "fill_type":
                fillType.setSummary(prefs.getString("fill_type", "MULTI"));
                break;

            case "ad_position_type":
                adPositionType.setSummary(prefs.getString("ad_position_type", "PRE"));
                break;

            case "duration":
                duration.setSummary(prefs.getString("duration", "15"));
                break;

            case "package_name":
                packageName.setSummary(prefs.getString("package_name", ""));
                break;

            case "epi_code":
                epiCode.setSummary(prefs.getString("epi_code", ""));
                break;

            case "channel_name":
                channelName.setSummary(prefs.getString("channel_name", ""));
                break;

            case "episode_name":
                episodeName.setSummary(prefs.getString("episode_name", ""));
                break;

            case "creator_id":
                creatorId.setSummary(prefs.getString("creator_id", ""));
                break;

            case "creator_name":
                creatorName.setSummary(prefs.getString("creator_name", ""));
                break;

            case "companion_size":
                boolean autoCompanionSize = prefs.getBoolean("companion_size", false);
                companionWidth.setEnabled(!autoCompanionSize);
                companionHeight.setEnabled(!autoCompanionSize);
                companionSize.setSummary(autoCompanionSize ? "자동" : "수동 설정");
                break;

            case "notification_title":
                notificationTitle.setSummary(prefs.getString("notification_title", getString(R.string.app_name)));
                break;

            case "notification_text":
                notificationText.setSummary(prefs.getString("notification_text", getString(R.string.app_name) + " 후원하는 광고 재생 중"));
                break;

            case "ad_request_delay":
                adRequestDelay.setSummary(prefs.getString("ad_request_delay", "0"));
                break;

        }
    }

    private EditTextPreference.OnBindEditTextListener setInputType(int type) {
        return editText -> editText.setInputType(type);
    }
}
