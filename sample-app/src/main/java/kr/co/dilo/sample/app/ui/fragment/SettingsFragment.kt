package kr.co.dilo.sample.app.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import androidx.preference.*
import kr.co.dilo.sample.app.R
import kr.co.dilo.sample.app.util.DiloSampleAppUtil
import kr.co.dilo.sdk.DiloUtil
import kr.co.dilo.sdk.RequestParam

/**
 * 설정 화면
 */
class SettingsFragment : PreferenceFragmentCompat() {

    private var prefs: SharedPreferences? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)

        val companionWidth:             EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_COMPANION_WIDTH)
        val companionHeight:            EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_COMPANION_HEIGHT)
        val productType:                    ListPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_PRODUCT_TYPE)
        val fillType:                       ListPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_FILL_TYPE)
        val adPositionType:                 ListPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_AD_POSITION_TYPE)
        val duration:                   EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_DURATION)
        val packageName:                EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_PACKAGE_NAME)
        val epiCode:                    EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_EPI_CODE)
        val channelName:                EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_CHANNEL_NAME)
        val episodeName:                EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_EPISODE_NAME)
        val creatorId:                  EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_CREATOR_ID)
        val creatorName:                EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_CREATOR_NAME)
        val diloAdTarget:                   ListPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_AD_TARGET)
        val companionSize:                SwitchPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_COMPANION_SIZE)
        val usePauseInNotification:       SwitchPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_USE_PAUSE_IN_NOTIFICATION)
        val useProgressBarInNotification: SwitchPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_USE_PROGRESS_BAR_IN_NOTIFICATION)
        val notificationTitle:          EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TITLE)
        val notificationText:           EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TEXT)
        val useBackground:                SwitchPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_USE_BACKGROUND)
        val adRequestDelay:             EditTextPreference? = findPreference(DiloSampleAppUtil.PREF_DILO_AD_REQUEST_DELAY)
        val sdkVersion:                         Preference? = findPreference(DiloSampleAppUtil.PREF_DILO_SDK_VERSION)

        sdkVersion?.summary = "${DiloUtil.DILO_SDK_VERSION} / ${DiloUtil.DILO_SDK_BUILD_TYPE.uppercase()}"

        prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        // 초기값 설정
        initProperty(companionWidth, DiloSampleAppUtil.PREF_DILO_COMPANION_WIDTH, "300")
        initProperty(companionHeight, DiloSampleAppUtil.PREF_DILO_COMPANION_HEIGHT, "300")
        initProperty(productType, DiloSampleAppUtil.PREF_DILO_PRODUCT_TYPE, RequestParam.ProductType.DILO_PLUS_ONLY.value)
        initProperty(fillType, DiloSampleAppUtil.PREF_DILO_FILL_TYPE, RequestParam.FillType.MULTI.value)
        initProperty(adPositionType, DiloSampleAppUtil.PREF_DILO_AD_POSITION_TYPE, RequestParam.AdPositionType.PRE.value)
        initProperty(duration, DiloSampleAppUtil.PREF_DILO_DURATION, "15")
        initProperty(packageName, DiloSampleAppUtil.PREF_DILO_PACKAGE_NAME, "com.queen.sampleapp")
        initProperty(epiCode, DiloSampleAppUtil.PREF_DILO_EPI_CODE, "test_live")
        initProperty(channelName, DiloSampleAppUtil.PREF_DILO_CHANNEL_NAME, "테스트 채널")
        initProperty(episodeName, DiloSampleAppUtil.PREF_DILO_EPISODE_NAME, "테스트 에피소드")
        initProperty(creatorId, DiloSampleAppUtil.PREF_DILO_CREATOR_ID, "테스터")
        initProperty(creatorName, DiloSampleAppUtil.PREF_DILO_CREATOR_NAME, "tester")
        initProperty(diloAdTarget, DiloSampleAppUtil.PREF_DILO_AD_TARGET, "PROD")
        initProperty(notificationTitle, DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TITLE, getString(R.string.app_name))
        initProperty(notificationText,DiloSampleAppUtil.PREF_DILO_NOTIFICATION_TEXT, "${getString(R.string.app_name)} 후원하는 광고 재생 중")
        initProperty(adRequestDelay, DiloSampleAppUtil.PREF_DILO_AD_REQUEST_DELAY, "0")

        // 숫자 유형만 받도록 설정
        companionWidth?.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        companionHeight?.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        duration?.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER))
        adRequestDelay?.setOnBindEditTextListener(setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
    }

    /**
     * EditText 입력 유형 설정
     */
    private fun setInputType(type: Int): EditTextPreference.OnBindEditTextListener {
        return EditTextPreference.OnBindEditTextListener { editText ->
            editText.inputType = type
            editText.setSelection(editText.length())
        }
    }

    /**
     * 프로퍼티 초기 값 설정
     */
    private fun initProperty(preference: Preference?, key: String, defValue: String) {
        // 초기값 설정
        val s = prefs!!.getString(key, defValue)
        preference?.summary = s
        prefHandle(preference, s)

        // 설정값 변경
        preference?.setOnPreferenceChangeListener { preference1, newValue ->
            val value = newValue as String
            preference1?.summary = value

            prefHandle(preference1, value)
            false
        }
    }

    /**
     * 특수 Preference 설정
     */
    private fun prefHandle(preference: Preference?, value: String?) {
        if (preference is ListPreference) {
            // ListPreference일 경우 변경된 값을 현재 선택값으로 변경
            preference.value = value
        } else if (preference is EditTextPreference) {
            // EditTextPreference 경우 변경된 값을 현재 텍스트값으로 변경
            preference.text = value
        }
    }
}
