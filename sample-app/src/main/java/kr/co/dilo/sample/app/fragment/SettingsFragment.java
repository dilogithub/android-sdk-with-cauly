package kr.co.dilo.sample.app.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import androidx.preference.*;
import kr.co.dilo.sample.app.R;
import kr.co.dilo.sample.app.util.DiloSampleAppUtil;
import kr.co.dilo.sdk.DiloUtil;
import kr.co.dilo.sdk.RequestParam;

/**
 * 설정 화면
 */
public class SettingsFragment extends PreferenceFragmentCompat {

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
    SwitchPreference   diloAdTarget;
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

        companionWidth               = findPreference(DiloSampleAppUtil.PREF_DILO_COMPANION_WIDTH);
        companionHeight              = findPreference(DiloSampleAppUtil.PREF_DILO_COMPANION_HEIGHT);
        productType                  = findPreference(DiloSampleAppUtil.PREF_DILO_PRODUCT_TYPE);
        fillType                     = findPreference(DiloSampleAppUtil.PREF_DILO_FILL_TYPE);
        adPositionType               = findPreference(DiloSampleAppUtil.PREF_DILO_AD_POSITION_TYPE);
        duration                     = findPreference(DiloSampleAppUtil.PREF_DILO_DURATION);
        packageName                  = findPreference(DiloSampleAppUtil.PREF_DILO_PACKAGE_NAME);
        epiCode                      = findPreference(DiloSampleAppUtil.PREF_DILO_EPI_CODE);
        channelName                  = findPreference(DiloSampleAppUtil.PREF_DILO_CHANNEL_NAME);
        episodeName                  = findPreference(DiloSampleAppUtil.PREF_DILO_EPISODE_NAME);
        creatorId                    = findPreference(DiloSampleAppUtil.PREF_DILO_CREATOR_ID);
        creatorName                  = findPreference(DiloSampleAppUtil.PREF_DILO_CREATOR_NAME);
        diloAdTarget                 = findPreference(DiloSampleAppUtil.PREF_DILO_AD_TARGET);
        companionSize                = findPreference(DiloSampleAppUtil.PREF_DILO_COMPANION_SIZE);
        usePauseInNotification       = findPreference(DiloSampleAppUtil.PREF_DILO_USE_PAUSE_IN_NOTIFICATION);
        useProgressBarInNotification = findPreference(DiloSampleAppUtil.PREF_DILO_USE_PROGRESS_BAR_IN_NOTIFICATION);
        notificationTitle            = findPreference(DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TITLE);
        notificationText             = findPreference(DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TEXT);
        useBackground                = findPreference(DiloSampleAppUtil.PREF_DILO_USE_BACKGROUND);
        adRequestDelay               = findPreference(DiloSampleAppUtil.PREF_DILO_AD_REQUEST_DELAY);
        sdkVersion                   = findPreference(DiloSampleAppUtil.PREF_DILO_SDK_VERSION);

        if (sdkVersion != null) {
            sdkVersion.setSummary(DiloUtil.getSDKVersion());
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // 초기값 설정
        initProperty(companionWidth, DiloSampleAppUtil.PREF_DILO_COMPANION_WIDTH, "300");
        initProperty(companionHeight, DiloSampleAppUtil.PREF_DILO_COMPANION_HEIGHT, "300");
        initProperty(productType, DiloSampleAppUtil.PREF_DILO_PRODUCT_TYPE, RequestParam.ProductType.DILO_PLUS_ONLY.getValue());
        initProperty(fillType, DiloSampleAppUtil.PREF_DILO_FILL_TYPE, RequestParam.FillType.MULTI.getValue());
        initProperty(adPositionType, DiloSampleAppUtil.PREF_DILO_AD_POSITION_TYPE, RequestParam.AdPositionType.PRE.getValue());
        initProperty(duration, DiloSampleAppUtil.PREF_DILO_DURATION, "15");
        initProperty(packageName, DiloSampleAppUtil.PREF_DILO_PACKAGE_NAME, "com.queen.sampleapp");
        initProperty(epiCode, DiloSampleAppUtil.PREF_DILO_EPI_CODE, "test_live");
        initProperty(channelName, DiloSampleAppUtil.PREF_DILO_CHANNEL_NAME, "테스트 채널");
        initProperty(episodeName, DiloSampleAppUtil.PREF_DILO_EPISODE_NAME, "테스트 에피소드");
        initProperty(creatorId, DiloSampleAppUtil.PREF_DILO_CREATOR_ID, "테스터");
        initProperty(creatorName, DiloSampleAppUtil.PREF_DILO_CREATOR_NAME, "tester");
        initProperty(notificationTitle, DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TITLE, getString(R.string.app_name));
        initProperty(notificationText, DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TEXT, getString(R.string.app_name) + " 후원하는 광고 재생 중");
        initProperty(adRequestDelay, DiloSampleAppUtil.PREF_DILO_AD_REQUEST_DELAY, "0");

        // 숫자 유형만 받도록 설정
        companionWidth.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        companionHeight.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        duration.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER));
        adRequestDelay.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
    }

    // EditText 입력 유형 설정
    private EditTextPreference.OnBindEditTextListener setInputType(int type) {
        return editText -> {
            editText.setInputType(type);
            editText.setSelection(editText.length());
        };
    }

    private void initProperty(Preference preference, String key, String defValue) {
        // 초기값 설정
        String s = prefs.getString(key, defValue);
        preference.setSummary(s);

        // 설정값 변경
        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
            String value = (String) newValue;
            preference1.setSummary(value);

            if (preference1 instanceof ListPreference) {
                // ListPreference일 경우 변경된 값을 현재 선택값으로 변경
                ((ListPreference) preference1).setValue(value);
            } else if (preference1 instanceof EditTextPreference) {
                // EditTextPreference 경우 변경된 값을 현재 텍스트값으로 변경
                ((EditTextPreference) preference1).setText(value);
            }
            return false;
        });
    }
}
